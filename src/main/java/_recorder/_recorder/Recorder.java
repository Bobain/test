package _recorder._recorder;


import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.xeiam.xchange.Exchange;
import com.xeiam.xchange.ExchangeFactory;
import com.xeiam.xchange.bitstamp.BitstampExchange;
import com.xeiam.xchange.currency.CurrencyPair;
import com.xeiam.xchange.dto.marketdata.OrderBook;
import com.xeiam.xchange.dto.marketdata.Trade;
import com.xeiam.xchange.dto.marketdata.Trades;
import com.xeiam.xchange.service.polling.marketdata.PollingMarketDataService;


/**
 * Recording a specified orderbook activity using rest API
 */
public final class Recorder {
	private final static class RecorderConfig {
		public final String marketToRecord;
		public final CurrencyPair currencyPairToRecord;
		public RecorderConfig(String mtr, CurrencyPair cptr) {
			this.marketToRecord = mtr;
			this.currencyPairToRecord = cptr;
		}
	}

	public final static RecorderConfig[] watchList = {
		new RecorderConfig(BitstampExchange.class.getName(), CurrencyPair.BTC_USD),
		new RecorderConfig(BitstampExchange.class.getName(), CurrencyPair.BTC_USD),
		new RecorderConfig(BitstampExchange.class.getName(), CurrencyPair.BTC_USD),
		new RecorderConfig(BitstampExchange.class.getName(), CurrencyPair.BTC_USD),
		new RecorderConfig(BitstampExchange.class.getName(), CurrencyPair.BTC_USD),
		new RecorderConfig(BitstampExchange.class.getName(), CurrencyPair.BTC_USD),
		new RecorderConfig(BitstampExchange.class.getName(), CurrencyPair.BTC_USD),
		new RecorderConfig(BitstampExchange.class.getName(), CurrencyPair.BTC_USD),
		new RecorderConfig(BitstampExchange.class.getName(), CurrencyPair.BTC_USD)
	};

	public final static void main(String[] args) throws Exception {
		int ind2record = 0;
		String outputDir = "/home/tonigor/btcdata";
		if (args.length > 0){
			outputDir = args[0];
			ind2record = Integer.parseInt(args[1]);
		}

		// Use the factory to get chosen exchange API using default settings
		String marketName =  Recorder.watchList[ind2record].marketToRecord;
		Exchange exchange = ExchangeFactory.INSTANCE.createExchange(marketName);

		// Interested in the public polling market data feed (no authentication)
		PollingMarketDataService marketDataService = exchange.getPollingMarketDataService();

		restAPIrecorder(outputDir, marketDataService, marketName, watchList[ind2record].currencyPairToRecord);
		// raw((BitstampMarketDataServiceRaw) marketDataService);

	}

	private final static void restAPIrecorder(String outputDir, PollingMarketDataService marketDataService, String marketName, CurrencyPair cP) throws Exception {
		outputDir += "/restAPI";
		// Outputdir should exist
		if(!new File(outputDir).exists()) {
            new File(outputDir).mkdirs();
        }
		
		// Formatting for filenames
		Date date = new Date();
		DateFormat dateFormat = new SimpleDateFormat("yyyy_MM_dd");
		String[] marketNameSplit = marketName.split("\\.");
		String shortMarketName = marketNameSplit[marketNameSplit.length-2];
		
		// Error Logging Management
		String errorFilename = outputDir + "/" + dateFormat.format(date) + "_" + shortMarketName + "_" + cP.toString().replaceAll("/", "_") + ".error";
		FileWriter fileWriterError = null;
		
		// Trades variables
		Trades trades = marketDataService.getTrades(cP);
		Trades oldTrades = trades;
		java.util.List<Trade> tradesUpdate = trades.getTrades();

		// orderbooks variables
		OrderBook oldOrderBook = marketDataService.getOrderBook(cP);
		OrderBook orderBook =  oldOrderBook;
		OrderBook orderBookUpdate = oldOrderBook;
		
		// Ouput file management
		String dataOutputDir = outputDir + "/" + cP.toString().replaceAll("/", "_") + "/" + shortMarketName; 
		if(!new File(dataOutputDir).exists()) {
			new File(dataOutputDir).mkdirs();
		}
		FileWriter fileWriter = new FileWriter(dataOutputDir + "/" + dateFormat.format(date)  + ".obat");
		BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);

		while (true) {
			try {
				if (tradesUpdate.size()>0) {
					bufferedWriter.write(xchangeTools.tradeListToString(tradesUpdate));
					bufferedWriter.newLine();
				}
				if (orderBookUpdate.getAsks().size()+orderBookUpdate.getBids().size()+tradesUpdate.size()>0) {
					bufferedWriter.write(xchangeTools.obToString(orderBookUpdate));
					bufferedWriter.newLine();
					bufferedWriter.flush();
				}

				// Waiting enough for not being banned
				Thread.sleep(2500);

				// Trades updates
				trades = marketDataService.getTrades(cP);
				tradesUpdate = xchangeTools.computeTradesUpdates(trades, oldTrades);
				oldTrades = trades;

				// Orderbook updates
				orderBook = marketDataService.getOrderBook(cP);
				orderBookUpdate = xchangeTools.computeObUpdates(orderBook, oldOrderBook);
				oldOrderBook = orderBook;
				
			} catch (Exception e) {
				if (fileWriterError == null) {
					fileWriterError = new FileWriter(errorFilename);
				}
				fileWriter.write("Error Timestamp=" + new Date().getTime() + "\n" + e.toString() + "\n");
				e.printStackTrace();
				//fileWriterError.close();
				//bufferedWriter.close();
			}
		}
	}
}
