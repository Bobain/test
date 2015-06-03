# java -cp /home/tonigor/btcdata/bin/recorder-3.0.1-SNAPSHOT.jar _recorder._recorder.MarketsMonitor 86410 /home/tonigor/btcdata /home/tonigor/btcdata/bin/recorder-3.0.1-SNAPSHOT.jar
timeout 86410 java -Djavax.net.ssl.trustStore=/home/tonigor/jssecacerts -cp /home/tonigor/btcdata/bin/recorder-3.0.1-SNAPSHOT.jar _recorder._recorder.MarketsMonitor /home/tonigor/btcdata
