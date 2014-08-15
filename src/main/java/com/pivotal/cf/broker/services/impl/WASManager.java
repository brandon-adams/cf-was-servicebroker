package com.pivotal.cf.broker.services.impl;

import java.io.InputStream;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import com.jcraft.jsch.*;
import com.pivotal.cf.broker.model.Plan;
import com.pivotal.cf.broker.model.ServiceInstance;
import com.pivotal.cf.broker.model.ServiceInstanceBinding;
import com.pivotal.cf.broker.repositories.PlanRepository;
import com.pivotal.cf.broker.repositories.ServiceInstanceRepository;
import com.pivotal.cf.broker.services.WASService;

@Service
public class WASManager implements WASService{
	
	private JSch shell;
	private Session session;
	private StringBuilder output;
	
	@Autowired
	private Environment env;
	
	@Autowired
	private PlanRepository planRepository;
	
	@Autowired
	private ServiceInstanceRepository serviceInstanceRepository;
	
	final Logger logger = LoggerFactory.getLogger(WASManager.class);
	
	public WASManager() {
		shell = new JSch();
		try {
			//shell.setKnownHosts("lib/known_hosts");
			//shell.addIdentity("src/main/resources/static/id_rsa");
			shell.addIdentity("/var/lib/tomcat/webapps/was-broker/WEB-INF/classes/static/id_rsa");
			session = shell.getSession("opstack", "192.168.0.126", 22);
			Properties config = new java.util.Properties();
			config.put("StrictHostKeyChecking", "no");
			session.setConfig(config);
			session.setPassword("Passw0rd!");
			session.connect();
			//System.out.println(WASManager.class.getProtectionDomain().getCodeSource().getLocation().getPath());
		} catch (JSchException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public boolean createProfile(Plan plan) {
		String profileName = plan.getMetadata().getOther().get("profilename");
		String nodeName = plan.getMetadata().getOther().get("nodename");
		String appNodeName = nodeName +"-AppNode";
		String cellName = profileName+"-Cell";
		String mngdNodeName = profileName+"-Manager";
		//if (new File("/opt/IBM/WebSphere/AppServer/profiles/"+profileName).exists()) return false;
		String profileStmt = env.getProperty("was.manageprofiles.location")
			   	 		+ " -create -profileName " +profileName
			   			 + " -profilePath " +env.getProperty("was.profiles.location")+profileName
			   			 + " -templatePath "+env.getProperty("was.templates.location")+"cell/dmgr"
			   			 + " -serverType DEPLOYMENT_MANAGER"
			   			 + " -appServerNodeName "+appNodeName
			   			 + " -nodeProfilePath " +env.getProperty("was.profiles.location")+nodeName
			   			 + " -hostName " +env.getProperty("was.host")
			   			 + " -cellName " +cellName
			   			 + " -nodeName " +mngdNodeName+";"
			   			 + " " +env.getProperty("was.profiles.location")+profileName+"/bin/startManager.sh";
		
		runCommand(profileStmt);
		
		String nodeStmt = env.getProperty("was.profiles.location")+profileName+"/bin/manageprofiles.sh"
 				 + " -create -profileName "+nodeName
 				 + " -profilePath "+env.getProperty("was.profiles.location")+nodeName
 				 + " -templatePath "+env.getProperty("was.templates.location")+"cell/default"
 				 + " -dmgrProfilePath " + env.getProperty("was.profiles.location")+profileName
 				 + " -cellName "+cellName
 				 + " -hostName 192.168.0.126"
 				 + " -nodeName "+mngdNodeName
 				 + " -appServerNodeName "+appNodeName
 				 + " -portsFile " + env.getProperty("was.profiles.location")+profileName+"/properties/portdef.props"
 				 + " -nodePortsFile " + env.getProperty("was.profiles.location")+profileName+"/properties/nodeportdef.props &&"
 				 //+ " SOAP_PORT=`cat "+env.getProperty("was.profiles.location")+profileName+"/properties/portdef.props"
 				 //+ " | grep -i soap | awk '{ print $1 }' | cut -d'=' -f2`;"
 				 + " "+env.getProperty("was.profiles.location")+nodeName+"/bin/startNode.sh";
		
		runCommand(nodeStmt);
		
		runCommand("cat "+env.getProperty("was.profiles.location")
				+profileName+"/properties/portdef.props"
  				+ " | grep -i adminhost | awk '{ print $1 }' | cut -d'=' -f2");
		String adminConsole = "http://"+env.getProperty("was.host")+":"+output.toString()+"/admin";
		
		plan.getMetadata().getOther().put("adminconsole", adminConsole);
		plan.getMetadata().getOther().put("appnodename", appNodeName);
		plan.getMetadata().getOther().put("cellname", cellName);
		plan.getMetadata().getOther().put("mngdnodename", mngdNodeName);
		plan.getMetadata().getOther().put("defaultserver", "server1");
		return true;
	}

	@Override
	public boolean deleteProfile(Plan plan) {
		String profileName = plan.getMetadata().getOther().get("profilename");
		String nodeName = plan.getMetadata().getOther().get("nodename");
		
		String nodeStmt = env.getProperty("was.profiles.location")+nodeName+"/bin/stopNode.sh;"
				+ " "+env.getProperty("was.profiles.location")+profileName+"/bin/manageprofiles.sh"
   				+ " -delete -profileName "+nodeName;
		
		runCommand(nodeStmt);
		//System.out.println(output.toString());
		//if (new File("/opt/IBM/WebSphere/AppServer/profiles/"+profileName).exists()) return false;
	   	String profileStmt = env.getProperty("was.profiles.location")+profileName+"/bin/stopManager.sh;"
	   	 		+ " " +env.getProperty("was.manageprofiles.location")+" -delete -profileName "+profileName;
	   	 		
	   	
	   	runCommand(profileStmt);
	   	String removeFiles = "rm -rf "+env.getProperty("was.profiles.location")+nodeName+";"
	   			+ " rm -rf " +env.getProperty("was.profiles.location")+profileName;
	   	runCommand(removeFiles);
	   	//System.out.println(output.toString());
		return true;
	}

	@Override
	public boolean createAppServer(ServiceInstance instance){
		//ServiceInstance instance = serviceInstanceRepository.findOne(binding.getServiceInstanceId());
		String profileName = instance.getConfig().get("profilename");
		String serverName = instance.getConfig().get("servername");
		String appNodeName = instance.getConfig().get("appnodename");
		String serverStmt = env.getProperty("was.profiles.location")+profileName+"/bin/wsadmin.sh -lang jython -f"
				+ " createAppServer.py " +appNodeName+" "+serverName;
		
		runCommand(serverStmt);
		System.out.println(output.toString());
		//instance.getConfig().put("serverinfo", output.toString());
		return true;
	}

	@Override
	public boolean deleteAppServer(ServiceInstance instance){
		String profileName = instance.getConfig().get("profilename");
		String serverName = instance.getConfig().get("servername");
		String appNodeName = instance.getConfig().get("appnodename");
		String serverStmt = env.getProperty("was.profiles.location")+profileName+"/bin/wsadmin.sh -lang jython -f"
				+ " deleteAppServer.py " +appNodeName+" "+serverName;
		
		return runCommand(serverStmt);
	}
	
	private boolean runCommand(String cmd){
		output = new StringBuilder();
		try {
			//session.connect();
			Channel channel = session.openChannel("exec");
			((ChannelExec) channel).setCommand(cmd);
			channel.setInputStream(null);
			((ChannelExec) channel).setErrStream(System.err);
			InputStream in = channel.getInputStream();
			channel.connect();
			
			byte[] tmp = new byte[1024];
			while (true) {
				while (in.available() > 0) {
					int i = in.read(tmp, 0, 1024);
					if (i < 0)
						break;
					output.append(new String(tmp, 0, i));
					System.out.print(new String(tmp, 0, i));
				}
				if (channel.isClosed()) {
					if (in.available() > 0)
						continue;
					System.out.println("exit-status: "
							+ channel.getExitStatus());
					break;
				}
				try {
					Thread.sleep(1000);
				} catch (Exception ee) {
					System.err.println(ee.getClass().getName() + ": "
							+ ee.getMessage());
					return false;
				}
			}
			channel.disconnect();
			//session.disconnect();
		} catch(Exception e){
			e.printStackTrace();
			return false;
		}
		//System.out.println(output.toString());
		return true;
	}
	
	/*public static void main(String[] args) {
		WASManager test1 = new WASManager();
		try {
			//ServiceInstance inst = new ServiceInstance("1", "2", "3", "4", "5", "6");
			//inst.getConfig().put("servername", "temp");
			//test1.serviceInstanceRepository.save(inst);
			//test1.createAppServer(inst);
			//test1.createProfile(null);
			test1.runCommand("ls /");
			//Thread.sleep(4000);
			//test1.deleteProfile(null);
			//test1.deleteAppServer("1");
		} catch (Exception e) {
			System.err.println(e.getClass().getName() + ": " + e.getMessage());
		}
		try {
			//test1.createProfile(null);
			//Thread.sleep(4000);
			//test1.deleteProfile(null);
			//test1.deleteAppServer("1");
		} catch (Exception e) {
			System.err.println(e.getClass().getName() + ": " + e.getMessage());
		}
	}*/
	
}
