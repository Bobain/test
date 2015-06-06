# cd /home/tonigor/JavaBTCproject/xchange-itbit
# mvn clean install

# the full project should build with success (i removed failed step, namely cointrader to get it), or you should decomment the two previous lines

# Rebuilding the full project is only needed when source code is modified (DO NOT DO THAT TOO OFTEN!)
# cd /home/tonigor/JavaBTCproject
# mvn clean install

cd /home/tonigor/JavaBTCproject/_recorder
mvn clean install
cp -R /home/tonigor/JavaBTCproject/_recorder/target/. /home/tonigor/btcdata/bin/
cd /home/tonigor
