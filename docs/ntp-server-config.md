# NTP server configuration

  To configure the NTP server as SAPS-engine needs, do as follow:
  
  ```
  1. bash -c ‘echo "America/Recife" > /etc/timezone’
  2. dpkg-reconfigure -f noninteractive tzdata
  3. apt-get update
  4. apt install -y ntp
  5. sed -i "/server 0.ubuntu.pool.ntp.org/d" /etc/ntp.conf
  6. sed -i "/server 1.ubuntu.pool.ntp.org/d" /etc/ntp.conf
  7. sed -i "/server 2.ubuntu.pool.ntp.org/d" /etc/ntp.conf
  8. sed -i "/server 3.ubuntu.pool.ntp.org/d" /etc/ntp.conf
  9. sed -i "/server ntp.ubuntu.com/d" /etc/ntp.conf
  10. bash -c ‘echo "server ntp.lsd.ufcg.edu.br" >> /etc/ntp.conf’
  11. service ntp restart
  ```
  
After this, the ntp server should restart and will be ready to run.
