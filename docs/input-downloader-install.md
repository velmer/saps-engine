# Install and Configure Input Downloader

## What is Input Downloader?

This component searches for new tasks from the catalogue, verifies if the requested computations isn't already in storage, if it isn't Input Downloader will then download the input data necessary for performing the processes and store it on NFS

## Dependencies

All components from saps depend on Docker to function, installation guide [here:](./container-install.md)

Before starting the Input Downloader container, first an NFS server needs to be installed, and configured. It implements the Temporary Storage component. The following steps must be followed:
	
  ```
  1. apt-get install nfs-kernel-server -y
  2. mkdir -p /local/exports
  3. echo "/local/exports *(rw, insecure , no_subtree_check, async, no_root_squash ) " >> /etc/exports
  4. service nfs-kernel-server restart
  5. mkdir -p /var/log/sebal-execution
  6. touch /var/log/sebal-execution/sebal-execution.log
  7. chmod 777 /var/log/sebal-execution/sebal-execution.log
  ```
  
Now the Docker can be started and Input Downloader container image can be pulled, and a container running this image can be started:

  ```
  1. docker pull fogbow/downloader-deploy
  2. docker run --name input-downloader --privileged -td fogbow/downloader-deploy
  3. docker exec input-downloader service docker start
  ```

With all software deployed, the Input Downloader can be customized by editing its configuration file, as shown below:

## Configure
With all software deployed, the Input Downloader can be customized by editing its configuration file, as shown below: 

  ```
  # Input Downloader database URL prefix (ex.: jdbc:postgresql://)
  datastore_url_prefix=

  # Input Downloader database ip (ask your administrator for this info)
  datastore_ip=

  # Input Downloader database port (ask your administrator for this info)
  datastore_port=

  # Input Downloader database name (ask your administrator for this info)
  datastore_name=

  # Input Downloader database driver (ask your administrator for this info)
  datastore_driver=

  # Input Downloader database user name (ask your administrator for this info)
  datastore_username=

  # Input Downloader database user password (ask your administrator for this info)
  datastore_password=

  # Input Downloader default volume size (tipically: 180MB)
  default_volume_size=

  # Input Downloader default downloader period (tipically: 600000)
  default_downloader_period=

  # NFS server export path (tipically: /local/exports )
  saps_export_path=

  # Path to store scenes (tipically: /home/ubuntu/results)
  saps_container_linked_path=

  # Max number of tasks to download (tipically: 33)
  max_tasks_to_download=

  # Max attempts trying to download a scene (tipically: 2)
  max_download_attempts=

  # Script used to download a scene (tipically: /home/ubuntu/run.sh) 
  container_script=

  # Max number of requests to USGS download link
  max_usgs_download_link_requests=10

  # Simultaneous downloads allowed
  max_simultaneous_download=1

  # USGS login URL
  usgs_login_url=https://ers.cr.usgs.gov/login/

  # USGS API URL
  usgs_json_url=https://earthexplorer.usgs.gov/inventory/json

  # USGS username (ask your network administrator for this info)
  usgs_username=

  # USGS password (ask your network administrator for this info)
  usgs_password=

  # Period to refresh USGS’ API key (tipically 300000)
  usgs_api_key_period=
  ```

The configuration file must be copied to the container:

  ```
  1. docker cp downloader.conf <container_id>:/home/ubuntu/saps-engine/config
  ```

The script used to start the Input Downloader (example available [here](../bin/start-input-downloader)) also needs to be edited accordingly:

  ```
  # SAPS Engine directory path (Usually /home/ubuntu/saps-engine)
  sebal_engine_dir_path=

  # The IP running the Input Downloader Software
  crawler_ip=

  # SSH Port
  crawler_ssh_port=

  # NFS Port
  crawler_nfs_port=

  # The federation member
  federation_member=
  ```

Then, the script start-input-downloader file must be copied to the container:

  ```
  1. docker cp start-input-downloader <container_id>:/home/ubuntu/saps-engine/bin
  ```

Finally, type the following command is used to run the Input Downloader:

  ```
  1. docker exec <container_id> cd /home/ubuntu/saps-engine && bash bin/start-input-downloader &
  ```
