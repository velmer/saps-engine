# Install and Configure Preprocessor

### What is Preprocessor
Preprocessor is the component of saps-engine responsible for executing commands/processes that are agnostic to which main algorith was choosen to run on a taks, for example, detection of clouds in a image

### Dependencies
All components from saps depend on Docker to function, installation guide [here](./container-install.md)

First of all, configure the timezone and NTP client as it's explained on [this link](./ntp-server-config.md)

Install and configure NFS Client Installation in the Host.
```
1. sudo apt-get update
2. sudo apt-get install nfs-common
3. sudo mkdir -p <path_local>
4. mount -t nfs <ip_nfs_server>:<path_nfs_server> <path_local>
```
After installing the NSF client, the environment is ready to pull the image of the Preprocessor component, and start a container that runs this image:
  ```
  1. docker pull fogbow/preprocessor
  2. docker run -td -v <nfs_directory>:<container_dir> <docker_user>/<docker_repository>:<docker_repository_tag>
  3. container_id=$(docker ps | grep  “fogbow/preprocessor" | awk '{print $1}')
  ```
### Configure
The configuration file of the Preprocessor component must be edited to customize its behavior:
```
# Catalogue database URL prefix (ex.: jdbc:postgresql://)
datastore_url_prefix=

# Catalogue database ip  (Catalogue info should be the same from Scheduler installation )
datastore_ip=

# Catalogue database port
datastore_port=

# Catalogue database name
datastore_name=

# Catalogue database driver
datastore_driver=

# Catalogue database user name
datastore_username=

# Catalogue database user password
datastore_password=

# Container Configuration #####
# Path NFS directory <nfs_directory> (tipically: /local/exports
saps_export_path=

# Path in the container (tipically: /home/ubuntu/results) 
# default : /tmp 
saps_container_linked_path=

# No Required Configuration ####
# Preprocessor Execution interval (ms) (tipically 600000 )
preprocessor_execution_period=
```
Again, the file must be copied to the container:
```
docker cp preprocessor.conf <container_id>:/home/ubuntu/saps-engine/config
```
The saps-engine/bin/start-preprocessor script must
also be configured:
```
# Preprocessor log file patch (choose any)
saps_engine_log_properties_path =

# Preprocessor engine target path (ex.: target/saps-engine-0.0.1-SNAPSHOT.jar:target/lib)
saps_engine_target_path =

# Preprocessor configuration file path (if you followed the tutorial until here it is
#/home/ubuntu/saps-engine/config
saps_engine_conf_path =
```
Then, the edited start-preprocessor script must be copied to the container:
```
docker cp start-preprocessor <container_id>:/home/ubuntu/saps-engine/bin
```
Finally, the Preprocessor is started using the following command:
```
docker exec -i <container_id> bash -c “cd /home/ubuntu/saps-engine && bash bin/start-preprocessor &”
```
