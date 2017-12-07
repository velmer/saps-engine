# Install and Configure Scheduler

## What is Scheduler
Scheduler is the component responsible for making the requisition of new computer nodes on the cloud, transfering the necessary files, executing and monitoring the processing of images

## Dependencies
All components from saps depend on Docker to function, installation guide [here](./container-install.md)

Before starting the Scheduler container, the Catalog database must be created, using the following commands

  ```
  1. apt-get update
  2. apt-get install postgresql
  3. su postgres
  4. psql -c "CREATE USER <user_name> WITH PASSWORD '<user_password>';"
  5. psql -c "CREATE DATABASE <database_name> OWNER <user_name>;"
  6. psql -c "GRANT ALL PRIVILEGES ON DATABASE <database_name> TO <user_name>;"
  7. exit
  8. sed -i 's/peer/md5/g' /etc/postgresql/<installed_version>/main/pg_hba.conf
  9. bash -c 'echo “host    all             all             0.0.0.0/0               md5” >> /etc/postgresql/<installed_version>/main/pg_hba.conf'
  10. sudo sed -i "$ a\listen_addresses = '*'" /etc/postgresql/<installed_version>/main/postgresql.conf
  11. service postgresql restart
  ```

After that, configure the timezone and NTP client as it's explained on [this link](./ntp-server-config.md)


After installed, your environment is ready to pull Scheduler’s Docker image.

  ```
  1. docker pull fogbow/scheduler
  2. docker run -td -v <local_database_dir>:<container_database_dir> fogbow/scheduler
  3. container_id=$(docker ps | grep  “fogbow/scheduler" | awk '{print $1}')
  ```

## Configure
Before starting the service, the Scheduler configuration file (example available [here](../examples/scheduler.conf.example)) needs to be edited to customize the behavior of the Scheduler component. We show below the properties found in the configuration file, and the values assigned to them

  ```
  # Catalogue database URL prefix (ex.: jdbc:postgresql://)
  datastore_url_prefix=

  # Catalogue database ip (All catalogue info should match the inputs on the installation above)
  datastore_ip=

  # Catalogue database port (All catalogue info should match the inputs on the installation above)
  datastore_port=

  # Catalogue database name (All catalogue info should match the inputs on the installation above)
  datastore_name=

  # Catalogue database driver (All catalogue info should match the inputs on the installation above)
  datastore_driver=

  # Catalogue database user name (All catalogue info should match the inputs on the installation above)
  datastore_username=

  # Catalogue database user password (All catalogue info should match the inputs on the installation above)
  datastore_password=

  # Worker spec file ##TODO (tipically config/workerSpec)
  infra_initial_specs_file_path=

  # Worker sandbox path (tipically: /tmp/sandbox )
  worker_sandbox=

  # Worker temporary raster directory (tipically: /mnt/rasterTmp)
  worker_raster_tmp_dir=

  # Worker run script path (tipically: /home/ubuntu/saps-engine/scripts/worker-run.sh)
  saps_worker_run_script_path=

  # Worker remote user (tipically: ubuntu)
  worker_remote_user=

  # NFS server export path (tipically: /local/exports)
  saps_export_path=

  # Worker mount point (tipically: /nfs )
  worker_mount_point=

  # Worker exit code file path (tipically: /home/fogbow/exit-check)
  remote_command_exit_path=

  # Blowout directory path (tipically: /home/ubuntu/blowout)
  blowout_dir_path=

  # Infrastructure order service time (tipically: 60000)
  infra_order_service_time=

  # Infrastructure resource service time (tipically: 40000)
  infra_resource_service_time=

  # Scheduler loop period (tipically: 60000)
  scheduler_period=

  # SAPS loop period (tipically: 60000)
  saps_execution_period=

  # Infrastructure specs block creating (tipically: false)
  infra_specs_block_creating=

  # Execution monitor period (tipically: 60000)
  execution_monitor_period=

  # Blowout Infrastructure manager implementation 
  # (tipically: org.fogbowcloud.blowout.infrastructure.manage.DefaultInfrastructureManager)
  impl_infra_manager_class_name=

  # Blowout Scheduler implementation
  # (tipically: org.fogbowcloud.blwout.core.StandardScheduler)
  impl_scheduler_class_name=

  # Blowout Pool implementation
  # (tipically: org.fogbowcloud.blowout.pool.DefaultBlowoutPool)
  impl_blowout_pool_class_name=

  # Infrastructure is elastic (tipically: true)
  infra_is_elastic=

  # Infrastructure Provider implementation
  # (tipically: org.fogbowcloud.blowout.infrastructure.provider.fogbow.FogbowInfrastructureProvider)
  infra_provider_class_name=

  # Infrastructure resource connection timeout (tipically: 20000)
  infra_resource_connection_timeout=

  # Infrastructure resource idle lifetime (tipically: 120000)
  infra_resource_idle_lifetime=

  # Maximum resource reuse (tipically: 10000)
  max_resource_reuse=

  # Maximum resource connection retries (tipically: 4)
  max_resource_connection_retry=

  # Infrastructure monitor period (tipically: 30000)
  infra_monitor_period=

  # Local Blowout command interpreter (tipically: /home/ubuntu/blowout/scripts/su_command)
  local_command_interpreter=

  # Token update time (tipically: 2)
  token_update_time=

  # Token update time unit (tipically: h)
  token_update_time_unit=

  # Blowout datastore URL (tipically: jdbc:sqlite:/tmp/blowout.db)
  blowout_datastore_url=
  ```

Scheduler must know from which Fogbow Manager the resources requests will be made. Currently, we have three plugins available to use in SAPS Engine: LDAPTokenUpdatePlugin, NAFTokenUpdatePlugin and KeystoneTokenUpdatePlugin. Depending on Manager implementation, the plugin chosen will change. To configure this, modify your scheduler.conf with

  ```
  # Fogbow infrastructure manager base URL (ask your network administrator for this info)
  infra_fogbow_manager_base_url=

  # Infrastructure authorization token plugin
  # for ldap (tipically: org.fogbowcloud.blowout.infrastructure.token.LDAPTokenUpdatePlugin)
  infra_auth_token_update_plugin=
  ```

To configure LDAP authentication:

  ```
  # LDAP Infrastructure user name
  fogbow.ldap.username=

  # LDAP Infrastructure user password
  fogbow.ldap.password=

  # LDAP Infrastructure authorization URL (ask your network admnistrator for this info)
  fogbow.ldap.auth.url=

  # LDAP Infrastructure base (ask your network admnistrator for this info)
  fogbow.ldap.base=

  # LDAP Infrastructure encrypt type (ask your network admnistrator for this info)
  auth_token_prop_ldap_encrypt_type=

  # LDAP Infrastructure private key 
  fogbow.ldap.private.key=

  # LDAP Infrastructure public key
  fogbow.ldap.public.key=
  ```

To configure NAF authentication:

  ```
  # NAF Infrastructure private key 
  auth_token_prop_naf_identity_private_key=

  # NAF Infrastructure public key
  auth_token_prop_naf_identity_public_key=

  # NAF Infrastructure user name
  auth_token_prop_naf_identity_token_username=

  # NAF Infrastructure user password
  auth_token_prop_naf_identity_token_password=
  ```

To configure Keystone authentication:

  ```
  # Keystone Infrastructure tenant name
  auth_token_prop_keystone_tenantname=

  # Keystone Infrastructure user password
  auth_token_prop_keystone_password=

  # Keystone Infrastructure user name
  auth_token_prop_keystone_username=

  # Keystone Infrastructure authentication URL (ask your network admnistrator for this info)
  auth_token_prop_keystone_auth_url=
  ```

Once the configuration file has been appropriately customized, it needs to be copied to the container:

  ```
  docker cp scheduler.conf <container_id>:/home/ubuntu/saps-engine/config
  ```

## Run
The script used to start the Scheduler (example available [here](../bin/start-scheduler)) also needs to be edited accordingly:

  ```
  # SAPS Engine directory path (Usually /home/ubuntu/saps-engine)
  saps_engine_dir_path=

  # Scheduler configuration file path
  saps_engine_conf_path=

  # Scheduler log file path
  saps_engine_log_properties_path=

  # Scheduler target file path (ex.: target/saps-engine-0.0.1-SNAPSHOT.jar:target/lib)
  saps_engine_target_path=

  # Local library path
  library_path=

  # Debug port
  debug_port=
  ```

Then, it needs to be copied to the container:

  ```
  docker cp start-scheduler <container_id>:/home/ubuntu/saps-engine/bin
  ```

Finally, run the Scheduler using:

  ```
  docker exec -i <container_id> bash -c “cd /home/ubuntu/saps-engine && bash bin/start-scheduler &”
  ```
