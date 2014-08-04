package com.pivotal.cf.broker.services;

import com.pivotal.cf.broker.model.CreateServiceInstanceRequest;
import com.pivotal.cf.broker.model.Plan;
import com.pivotal.cf.broker.model.ServiceDefinition;
import com.pivotal.cf.broker.model.ServiceInstance;
import com.pivotal.cf.broker.model.ServiceInstanceBinding;

public interface WASService {
	public boolean createProfile(ServiceInstance instance);
	public boolean deleteProfile(ServiceInstance instance);
	public boolean createAppServer(ServiceInstanceBinding binding);
	public boolean deleteAppServer(ServiceInstanceBinding binding);
	
	//public boolean createUser(ServiceInstanceBinding binding);
	//public boolean deleteUser(ServiceInstanceBinding binding);
}
