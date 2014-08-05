package com.pivotal.cf.broker.services.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import com.pivotal.cf.broker.exceptions.EntityNotFoundException;
import com.pivotal.cf.broker.model.CreateServiceInstanceRequest;
import com.pivotal.cf.broker.model.Plan;
import com.pivotal.cf.broker.model.ServiceDefinition;
import com.pivotal.cf.broker.model.ServiceInstance;
import com.pivotal.cf.broker.model.ServiceInstanceBinding;
import com.pivotal.cf.broker.model.ServiceInstanceBindingRequest;
import com.pivotal.cf.broker.repositories.PlanRepository;
import com.pivotal.cf.broker.repositories.ServiceDefinitionRepository;
import com.pivotal.cf.broker.repositories.ServiceInstanceBindingRepository;
import com.pivotal.cf.broker.repositories.ServiceInstanceRepository;
import com.pivotal.cf.broker.services.BaseService;
import com.pivotal.cf.broker.services.WASService;
import com.pivotal.cf.broker.services.ServiceManagement;
import com.pivotal.cf.broker.utils.StringUtils;

@Service
public class ServiceManagementImpl extends BaseService implements ServiceManagement {
	@Autowired
	private PlanRepository planRepository;
	
	@Autowired
	private ServiceDefinitionRepository serviceRepository;
	
	@Autowired 
	private ServiceInstanceRepository serviceInstanceRepository;
	
	@Autowired 
	private ServiceInstanceBindingRepository bindingRepository;
	
	@Autowired
	private WASManager wasManager;
	
	@Autowired
	private Environment env;
	
	@Override
	public ServiceInstance createInstance(CreateServiceInstanceRequest serviceRequest) {
		ServiceDefinition serviceDefinition = serviceRepository.findOne(serviceRequest.getServiceDefinitionId());
		if(serviceDefinition == null){
			throw new IllegalArgumentException("Service definition not found: " + serviceRequest.getServiceDefinitionId());
		}
		Plan plan = planRepository.findOne(serviceRequest.getPlanId());
		if(plan == null){
			throw new IllegalArgumentException("Invalid plan identifier");
		}
		if(serviceInstanceRepository.exists(serviceRequest.getServiceInstanceId())){
			throw new IllegalStateException("There's already an instance of this service");
		}
		ServiceInstance instance = new ServiceInstance(serviceRequest.getServiceInstanceId(), serviceDefinition.getId(), plan.getId(), serviceRequest.getOrganizationGuid(), serviceRequest.getSpaceGuid(), "");
		//String profileName = StringUtils.randomString(10);
		//String nodeName = StringUtils.randomString(10);
		Map<String,String> config = new HashMap<>();
		config.put("profilename",plan.getMetadata().getOther().get("profilename"));
		config.put("nodename",plan.getMetadata().getOther().get("nodename"));
		instance.setConfig(config);
		Map<String,Object> model = new HashMap<>();
		model.put("plan",plan);
		model.put("instance",instance);
		wasManager.createAppServer(instance);
		instance = serviceInstanceRepository.save(instance);
		return instance;
	}

	@Override
	public boolean removeServiceInstance(String serviceInstanceId) {
		if(!serviceInstanceRepository.exists(serviceInstanceId)){
			return false;
		}
		if(bindingRepository.countByServiceInstanceId(serviceInstanceId) > 0){
			throw new IllegalStateException("Can not delete service instance, there are still apps bound to it");
		}
		ServiceInstance instance = serviceInstanceRepository.findOne(serviceInstanceId);
		Map<String,Object> model = new HashMap<>();
		model.put("instance",instance);
		wasManager.deleteAppServer(instance);
		serviceInstanceRepository.delete(serviceInstanceId);
		return true;
	}

	@Override
	public List<ServiceInstance> listInstances() {
		return makeCollection(serviceInstanceRepository.findAll());
	}

	@Override
	public ServiceInstanceBinding createInstanceBinding(ServiceInstanceBindingRequest bindingRequest) {
		if(bindingRepository.exists(bindingRequest.getBindingId())){
			throw new IllegalStateException("Binding Already exists");
		}
		
		Map<String, String> credentials = new HashMap<>();
		credentials.put("hostname", env.getProperty("was.host"));
		//credentials.put("port", env.getProperty("was.port"));
		ServiceInstanceBinding binding = new ServiceInstanceBinding();
		binding.setId(bindingRequest.getBindingId());
		binding.setServiceInstanceId(bindingRequest.getInstanceId());
		binding.setAppGuid(bindingRequest.getAppGuid());
		ServiceInstance instance = serviceInstanceRepository.findOne(bindingRequest.getInstanceId());
		Plan plan = planRepository.findOne(bindingRequest.getPlanId());
		credentials.put("profilename",instance.getConfig().get("profilename"));
		credentials.put("nodename",instance.getConfig().get("nodename"));
		binding.setCredentials(credentials);
		Map<String,Object> model = new HashMap<>();
		model.put("plan",plan);
		model.put("binding",binding);
		model.put("instance",instance);
		//templateService.execute("binding/create.ftl", model);////////////////////////////////////////////
		//wasManager.createAppServer(binding);
		binding = bindingRepository.save(binding);
		return binding;
	}

	@Override
	public boolean removeBinding(String serviceBindingId) {
		ServiceInstanceBinding binding = bindingRepository.findOne(serviceBindingId);
		if(binding == null){
			return false;
		}
		Map<String,Object> model = new HashMap<>();
		model.put("binding",binding);
		//templateService.execute("binding/delete.ftl", model);//////////////////////////////////////////////
		//wasManager.deleteAppServer(binding);
		bindingRepository.delete(binding);
		return true;
	}

	@Override
	public List<ServiceInstanceBinding> listBindings() {
		return makeCollection(bindingRepository.findAll());
	}

}
