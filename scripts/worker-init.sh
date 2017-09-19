#!/bin/bash

BIN_INIT_SCRIPT="bin/init.sh"
IMAGES_DIR_NAME=images
RESULTS_DIR_NAME=results
CONTAINER_OUT_DIR=/home/ubuntu/$RESULTS_DIR_NAME
PROCESS_OUTPUT=

# This function removes all garbage from tmp
function tmpGarbageCollect {
  count=`ls -1 /tmp/Rtmp* 2>/dev/null | wc -l`
  if [ $count != 0 ]
  then 
    sudo rm -r /tmp/Rtmp*
  fi
}

# This function mounts exports dir from NFS server
function mountExportsDir {
  if [ ! -d ${SEBAL_MOUNT_POINT} ]
  then
    sudo mkdir -p ${SEBAL_MOUNT_POINT}
  fi

  if grep -qs "${SEBAL_MOUNT_POINT}" /proc/mounts;
  then
    echo "Directory ${SEBAL_MOUNT_POINT} already mounted."
  else
    echo "Directory ${SEBAL_MOUNT_POINT} not mounted yet...proceeding to mount"
    sudo mount -t nfs -o proto=tcp,port=${NFS_SERVER_PORT} ${NFS_SERVER_IP}:${VOLUME_EXPORT_PATH} ${SEBAL_MOUNT_POINT}
  fi
}

# This function downloads container image and prepare container to execution
function prepareDockerContainer {
  cd ${SANDBOX}

  echo "Pulling docker image from ${CONTAINER_REPOSITORY}:${CONTAINER_TAG}"
  docker pull ${CONTAINER_REPOSITORY}:${CONTAINER_TAG}

  if [ "$(docker ps -aq -f status=exited -f name=${CONTAINER_TAG})" ] || [ "$(docker ps -q -f name=${CONTAINER_TAG})" ]
  then
    # cleanup
    docker rm ${CONTAINER_TAG}
  fi

  docker run -d -v ${SEBAL_MOUNT_POINT}:${SEBAL_MOUNT_POINT} ${CONTAINER_REPOSITORY}:${CONTAINER_TAG} tailf /dev/null
}

function garbageCollect {
  DIRECTORY=${SEBAL_MOUNT_POINT}/$RESULTS_DIR_NAME/${IMAGE_NEW_COLLECTION_NAME}
  if [ -d "$DIRECTORY" ]; then
    shopt -s nullglob dotglob     # To include hidden files
    files=($DIRECTORY/*)
    if [ ${#files[@]} -gt 0 ];
    then
      echo "Directory contains garbage...cleanning it"
      sudo rm ${SEBAL_MOUNT_POINT}/$RESULTS_DIR_NAME/${IMAGE_NEW_COLLECTION_NAME}/*
    fi
  fi
}

# This function executes user's init script
function executeInitScript {
  cd ${SANDBOX}

  CONTAINER_ID=$(docker ps | grep "${CONTAINER_REPOSITORY}:${CONTAINER_TAG}" | awk '{print $1}')

  docker exec $CONTAINER_ID bash -x $BIN_INIT_SCRIPT
}

function checkProcessOutput {
  PROCESS_OUTPUT=$?

  if [ $PROCESS_OUTPUT -ne 0 ]
  then
    finally
  fi
}

# This function ends the script
function finally {
  echo $PROCESS_OUTPUT > ${REMOTE_COMMAND_EXIT_PATH}
  exit $PROCESS_OUTPUT
}

tmpGarbageCollect
checkProcessOutput
mountExportsDir
checkProcessOutput
prepareDockerContainer
checkProcessOutput
garbageCollect
checkProcessOutput
executeInitScript
checkProcessOutput
finally
