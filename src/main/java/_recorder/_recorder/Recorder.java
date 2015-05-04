package _recorder._recorder;


import java.io.BufferedWriter;
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
		if (args.length > 0){
			ind2record = Integer.parseInt(args[0]);
		}

		// Use the factory to get chosen exchange API using default settings
		String marketName =  Recorder.watchList[ind2record].marketToRecord;
		Exchange exchange = ExchangeFactory.INSTANCE.createExchange(marketName);

		// Interested in the public polling market data feed (no authentication)
		PollingMarketDataService marketDataService = exchange.getPollingMarketDataService();

		generic("/home/tonigor/btcdata", marketDataService, marketName, watchList[ind2record].currencyPairToRecord);
		// raw((BitstampMarketDataServiceRaw) marketDataService);

	}

	private final static void generic(String outputDir, PollingMarketDataService marketDataService, String marketName, CurrencyPair cP) throws Exception {
		// Trades variables
		Trades trades = marketDataService.getTrades(cP);
		Trades oldTrades = trades;
		java.util.List<Trade> tradesUpdate = trades.getTrades();

		// orderbooks variables
		OrderBook oldOrderBook = marketDataService.getOrderBook(cP);
		OrderBook orderBook =  oldOrderBook;
		OrderBook orderBookUpdate = oldOrderBook;
		Date date = new Date();

		// Ouput file management
		DateFormat dateFormat = new SimpleDateFormat("yyyy_MM_dd");
		System.out.println();
		FileWriter fileWriter = new FileWriter(outputDir + "/" + dateFormat.format(date) + "_" + marketName + "_" + cP.toString().replaceAll("/", "_") + ".obat");
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

			} catch (InterruptedException e) {
				e.printStackTrace();
				bufferedWriter.close();
			}
		} 
	}
}
