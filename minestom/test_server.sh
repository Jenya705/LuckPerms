mkdir ./test_server
cd ./test_server
wget -O server.jar https://github.com/Project-Cepi/Sabre/releases/download/latest/sabre-1.0.0-all.jar
echo 'java -Xms2G -Xmx4G -jar server.jar' > start.cmd
echo 'java -Xms2G -Xmx4G -jar server.jar' > start.sh
chmod +x ./start.sh