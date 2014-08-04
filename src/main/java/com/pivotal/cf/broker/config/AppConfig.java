package com.pivotal.cf.broker.config;

import javax.sql.DataSource;

import org.apache.tomcat.jdbc.pool.PoolProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.orm.jpa.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.Environment;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import com.pivotal.cf.broker.model.Plan;

@Configuration
@ComponentScan(basePackages = "com.pivotal.cf.broker")
@EnableJpaRepositories(basePackageClasses = Plan.class)
@EnableTransactionManagement
@EntityScan(basePackageClasses=Plan.class)
public class AppConfig {

	@Autowired
	private Environment env;

	/*@Bean
	@Primary
	public DataSource datasource() throws Exception {
		PoolProperties p = new PoolProperties();
		p.setDriverClassName(env.getProperty("broker.datasource.driverClassName"));
		p.setUsername(env.getProperty("broker.datasource.username"));
		p.setPassword(env.getProperty("broker.datasource.password"));
		p.setUrl(env.getProperty("broker.datasource.url"));
		p.setMaxActive(Integer.valueOf(env.getProperty("broker.datasource.max-active")));
		p.setMinIdle(Integer.valueOf(env.getProperty("broker.datasource.min-idle")));
		p.setMaxIdle(Integer.valueOf(env.getProperty("broker.datasource.max-idle")));
		p.setInitialSize(Integer.valueOf(env.getProperty("broker.datasource.initial-size")));
		org.apache.tomcat.jdbc.pool.DataSource ds = new org.apache.tomcat.jdbc.pool.DataSource();
		ds.setPoolProperties(p);
		return ds;
	}*/
	
	@Bean
	@Primary
	public DataSource datasource() throws Exception {
		PoolProperties p = new PoolProperties();
		
		p.setDriverClassName(env.getProperty("broker.mysql.driverClassName"));
		p.setUsername(env.getProperty("broker.mysql.username"));
		p.setPassword(env.getProperty("broker.mysql.password"));
		p.setUrl(env.getProperty("broker.mysql.url"));
		//p.setMaxActive(Integer.valueOf(env.getProperty("broker.datasource.max-active")));
		//p.setMinIdle(Integer.valueOf(env.getProperty("broker.datasource.min-idle")));
		//p.setMaxIdle(Integer.valueOf(env.getProperty("broker.datasource.max-idle")));
		//p.setInitialSize(Integer.valueOf(env.getProperty("broker.datasource.initial-size")));
		org.apache.tomcat.jdbc.pool.DataSource ds = new org.apache.tomcat.jdbc.pool.DataSource();
		ds.setPoolProperties(p);
		return ds;
	}
	
	@Bean
	public LocalContainerEntityManagerFactoryBean entityManagerFactory() throws ClassNotFoundException, IllegalAccessException, InstantiationException {
	    //Class.forName("com.mysql.jdbc.Driver").newInstance();
	    LocalContainerEntityManagerFactoryBean localContainerEntityManagerFactoryBean = new LocalContainerEntityManagerFactoryBean();
	    HibernateJpaVendorAdapter hibernateJpaVendorAdapter = new HibernateJpaVendorAdapter();
	    hibernateJpaVendorAdapter.setDatabasePlatform("org.hibernate.dialect.MySQLDialect");
	    hibernateJpaVendorAdapter.setShowSql(true);
	    localContainerEntityManagerFactoryBean.setJpaVendorAdapter(hibernateJpaVendorAdapter);
	    return localContainerEntityManagerFactoryBean;
	}
	
	@Bean
	JpaTransactionManager jpaTransactionManager(LocalContainerEntityManagerFactoryBean localContainerEntityManagerFactoryBean) {
	    JpaTransactionManager jpaTransactionManager = new JpaTransactionManager();
	    jpaTransactionManager.setEntityManagerFactory(localContainerEntityManagerFactoryBean.getObject());
	    return jpaTransactionManager;
	}

}
