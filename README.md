# Lost Isle Readme


## Deploying the server

Start mongodb:
```
cd /usr/bin/
sudo ./mongod --bind_ip 127.0.0.1 --fork --logpath /var/log/mongod.log
```
Wait until it has succeeded.

Start the server:
```
cd /usr/share/lostisle/lost-rsps
sudo nohup ./run.sh &
```