package com.pivotal.cf.broker.services;

import com.pivotal.cf.broker.model.CreateServiceInstanceRequest;
import com.pivotal.cf.broker.model.Plan;
import com.pivotal.cf.broker.model.ServiceDefinition;
import com.pivotal.cf.broker.model.ServiceInstance;
import com.pivotal.cf.broker.model.ServiceInstanceBinding;

public interface WASService {
	public boolean createProfile(Plan plan);
	public boolean deleteProfile(Plan plan);
	public boolean createAppServer(ServiceInstance instance);
	public boolean deleteAppServer(ServiceInstance instance);
	
	//public boolean createUser(ServiceInstanceBinding binding);
	//public boolean deleteUser(ServiceInstanceBinding binding);
}
