# SAPS Engine
## What is SAPS Engine?
  SAPS Engine is a tool created to provide dynamic access to SEBAL algorithm using computational resources obtained through a multi-cloud environment federated by the [Fogbow Middleware](http://www.fogbowcloud.org) (to install it, follow the instructions [here](http://www.fogbowcloud.org/the-big-picture.html)).
  
  SAPS Engine has seven main components:
  - **Submission Dispatcher**: The User interface, where the user can submit/remove new tasks.
  - **Task Catalogue**: Stores information of [LANDSAT](https://landsat.usgs.gov/) task data and its execution.
  - **Input Downloader**: Searches at **Task Catalogue** for LANDSAT new tasks, then this component assumes a FTP  Server role and downloads and stores image data into a NFS repository.
  - **Pre Processor**: Search at **Task Catalogue** for LANDSAT tasks already downloaded by **Input Downloader** for pre processing treatment, if necessary, before it arrives to **Scheduler**.
 - **Scheduler**: Orders resources as needed, then schedules tasks to **Worker Nodes**, which performs the actual processing.
  - **Worker Node**: Receives a task from **Scheduler** and executes it. The execution consists processing an image and then storing data at the NFS Server.
  - **Archiver**: Search at **Task Catalogue** for tasks that finished execution and transfers all task data from the NFS Server to a permanent storage. After that, **Input Downloader** is able to detect if the task was archived, so it can remove all task files from its own local repository.

## Install and Deploy
### Install Docker CE
SAPS componentes are deployed as Docker containers. Thus, before proper installing them, Docker needs to be installed in the virtual machines provisioned to run the SAPS service. 

To install Docker in a Debian based virtual machine follow the instructions provided [here](docs/container-install.md).

Once Docker is installed, the SAPS components can be deployed by pulling the container images available in the service’s repository. In the following, we show how this can be done, for each SAPS component, as well as the necessary customizations made for these components.

### SAPS Components Installation
* [Dispacher/Dashboard](docs/dispacher-install.md)
* [Input Downloader](docs/input-downloader-install.md)
* [Pre Processor](docs/preprocessor-install.md)
* [Scheduler](docs/scheduler-install.md)
* [Archiver](docs/archiver-install.md)
