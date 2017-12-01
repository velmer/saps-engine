# Install and Configure Archiver

## What is Archiver
The component responsible for permanent storage of Task data (input and output) and meta-data
## Dependencies
First of all, configure the timezone and NTP client as [follows](./ntp-server-config.md)


After this, the Docker image of the Archiver component can be pulled, and a container running this image can be started, using the following commands:

  ```
  1. docker pull fogbow/archiver
  2. docker run -td -v <nfs_directory>:<container_dir> <docker_user>/<docker_repository>:<docker_repository_tag>
  3. container_id=$(docker ps | grep “fogbow/archiver" | awk '{print $1}')
  ```

## Configure
The Archiver component can also be customized through its configuration file (example available [here](../examples/archiver.conf.example)):

  ```
  # Catalogue database URL prefix (ex.: jdbc:postgresql://)
  datastore_url_prefix=

  # Catalogue database ip
  datastore_ip=

  # Catalogue database port (ask your admnistrator for the following info)
  datastore_port=

  # Catalogue database name
  datastore_name=

  # Catalogue database driver
  datastore_driver=

  # Catalogue database user name
  datastore_username=

  # Catalogue database user password
  datastore_password=

  # Archiver SFTP script path
  saps_sftp_script_path=

  # Default FTP server user
  default_ftp_server_user=

  # Default FTP server port
  default_ftp_server_port=

  # FTP server export path
  saps_export_path=

  # Local files path
  local_input_output_path=

  # SAPS execution period
  saps_execution_period=

  # Default Archiver loop period
  default_archiver_period=

  # Swift container name
  swift_container_name=

  # Swift input pseudo folder prefix
  swift_input_pseud_folder_prefix=

  # Swift output pseudo folder prefix
  swift_output_pseud_folder_prefix=

  # Swift user name
  swift_username=

  # Swift user password
  swift_password=

  # Swift tenant id
  swift_tenant_id=

  # Swift tenant name
  swift_tenant_name=

  # Swift authorization URL
  swift_auth_url=

  # Keystone V3 project id
  fogbow.keystonev3.project.id=

  # Keystone V3 user id
  fogbow.keystonev3.user.id=

  # Keystone V3 user password
  fogbow.keystonev3.password=

  # Keystone V3 authorization URL
  fogbow.keystonev3.auth.url=

  # Keystone V3 Swift authorization URL
  fogbow.keystonev3.swift.url=

  # Keystone V3 Swift token update period
  fogbow.keystonev3.swift.token.update.period=

  # Fogbow-cli directory path
  fogbow_cli_path=
  ```

Once edited, the configuration file needs to be copied to the container:

  ```
  docker cp archiver.conf <container_id>:/home/ubuntu/saps-engine/config
  ```

## Run
Before running the Archiver, the saps-engine/bin/start-archiver configuration file (example available [here](../bin/start-archiver)) also needs to be edited.

  ```
  # SAPS Engine directory path (Usually /home/ubuntu/saps-engine)
  saps_engine_dir_path=

  # Scheduler configuration file path (if you followed the tutorial until here it is 
#/home/ubuntu/saps-engine/config  
saps_engine_conf_path=

  # Scheduler log file path (choose any)
  saps_engine_log_properties_path=

  # Scheduler target file path (ex.: target/saps-engine-0.0.1-SNAPSHOT.jar:target/lib)
  saps_engine_target_path=

  # Local library path (ex: /usr/lib
  library_path=

  # Debug port (ask your network administrator for this info)
  debug_port=
  ```

Then, it needs to be copied to the container:

  ```
  docker cp start-archiver <container_id>:/home/ubuntu/saps-engine/bin
  ```

Finally, run the Archiver using:

  ```
  docker exec <container_id> bash -c “cd /home/ubuntu/saps-engine && bash bin/start-archiver &”
  ```


