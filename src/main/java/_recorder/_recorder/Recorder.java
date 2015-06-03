package _recorder._recorder;


import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import com.xeiam.xchange.Exchange;
import com.xeiam.xchange.ExchangeFactory;
import com.xeiam.xchange.bitbay.BitbayExchange;
import com.xeiam.xchange.bitcurex.BitcurexExchange;
import com.xeiam.xchange.bitfinex.v1.BitfinexExchange;
import com.xeiam.xchange.bitstamp.BitstampExchange;
import com.xeiam.xchange.btccentral.BTCCentralExchange;
import com.xeiam.xchange.btcchina.BTCChinaExchange;
import com.xeiam.xchange.btce.v3.BTCEExchange;
import com.xeiam.xchange.clevercoin.CleverCoinExchange;
import com.xeiam.xchange.currency.CurrencyPair;
import com.xeiam.xchange.dto.marketdata.OrderBook;
import com.xeiam.xchange.dto.marketdata.Trade;
import com.xeiam.xchange.dto.marketdata.Trades;
import com.xeiam.xchange.exceptions.ExchangeException;
import com.xeiam.xchange.exceptions.NotAvailableFromExchangeException;
import com.xeiam.xchange.exceptions.NotYetImplementedForExchangeException;
import com.xeiam.xchange.hitbtc.HitbtcExchange;
import com.xeiam.xchange.itbit.v1.ItBitExchange;
import com.xeiam.xchange.kraken.KrakenExchange;
import com.xeiam.xchange.mercadobitcoin.MercadoBitcoinExchange;
import com.xeiam.xchange.okcoin.OkCoinExchange;
import com.xeiam.xchange.service.polling.marketdata.PollingMarketDataService;
import com.xeiam.xchange.virtex.v2.VirtExExchange;


/**
 * Recording a specified orderbook activity using rest API
 */
public final class Recorder extends Thread {
	public final static class RecorderConfig {
		public final String marketToRecord;
		public final CurrencyPair currencyPairToRecord;
		public final double requestWaitSeconds;
		public Boolean[] enableServices;
		public RecorderConfig(String mtr, CurrencyPair cptr, Double rws, Boolean[] es) {
			this.marketToRecord = mtr;
			this.currencyPairToRecord = cptr;
			this.requestWaitSeconds = rws;
			this.enableServices = es;
		}

	}

	public RecorderConfig settings;
	public String outputDir;
	private OrderBook orderBook;
	private Trades trades;
	private Trades oldTrades;
	private double latency;
	private double updDur;
	public String shortMarketName;
	public String marketName;
	public String errorFilename;
	public FileWriter fileWriterError = null;
	public long lastNbTrades = -1;
	private double requestWaitSeconds;

	public void plotOb() {
		xchangeTools.plotOB(this.orderBook, this.shortMarketName + " " + this.settings.currencyPairToRecord.toString());
	}
	
	public Trades getTrade(PollingMarketDataService marketDataService, CurrencyPair cP) 
			throws ExchangeException, NotAvailableFromExchangeException, NotYetImplementedForExchangeException, IOException{
		Trades t;
		// if (this.oldTrades.getlastID() == 0) {
			t = marketDataService.getTrades(cP);
		//} else {
			//t = marketDataService.getTrades(cP, oldTrades.getlastID());
		//}
		t.getTrades().removeAll(t.getTrades().subList(0, Math.max(0, t.getTrades().size()-100)));
		return t;
	}
	
	public void printErrorAndWait(Exception e) throws InterruptedException {
		if (fileWriterError == null) {
			try {
				fileWriterError = new FileWriter(errorFilename);
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
		try {
			fileWriterError.write("Error Timestamp=" + new Date().getTime() + "\n" 
					+ xchangeTools.getExceptionString(e) + "\n");
			fileWriterError.flush();
		} catch (IOException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
		e.printStackTrace();
		// Waiting enough for not being banned : An error will most probably occur when requesting data
		Thread.sleep((long) (2200*requestWaitSeconds));
		
	}
	
	public void run() {
		CurrencyPair cP = this.settings.currencyPairToRecord;
		requestWaitSeconds = this.settings.requestWaitSeconds;
		Boolean[] enableServices = this.settings.enableServices;
		
		long newTs = new Date().getTime();
		long lastTs = newTs;
		

		Exchange exchange = ExchangeFactory.INSTANCE.createExchange(marketName);

		// Interested in the public polling market data feed (no authentication)
		PollingMarketDataService marketDataService = exchange.getPollingMarketDataService();
		String oDir = outputDir + "/restAPI";
		// Outputdir should exist
		if(!new File(oDir).exists()) {
			new File(oDir).mkdirs();
		}

		// Formatting for filenames
		Date date = new Date();
		DateFormat dateFormat = new SimpleDateFormat("yyyy_MM_dd");


		// Error Logging Management
		errorFilename = oDir + "/" + dateFormat.format(date) + "_" + shortMarketName + "_" + cP.toString().replaceAll("/", "_") + ".error";
		this.fileWriterError = null;


		// Trades variables
		this.trades = null;
		oldTrades = null;
		java.util.List<Trade> tradesUpdate = null;
		// orderbooks variables
		OrderBook oldOrderBook = null;
		this.orderBook = null;
		OrderBook orderBookUpdate = null;

		// Ouput file management
		String dataOutputDir = oDir + "/" + cP.toString().replaceAll("/", "_") + "/" + shortMarketName; 
		if(!new File(dataOutputDir).exists()) {
			new File(dataOutputDir).mkdirs();
		}
		FileWriter fileWriter = null;
		try {
			fileWriter = new FileWriter(dataOutputDir + "/" + dateFormat.format(date)  + ".obat");
		} catch (IOException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
		BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);

		while ((this.trades == null && enableServices[0]) 
				|| (enableServices[1] && oldOrderBook == null)){
			try {
				if (enableServices[0]) {
					this.trades = getTrade(marketDataService, cP);
					oldTrades = this.trades;
					tradesUpdate = this.trades.getTrades();
				}


				if (enableServices[1]) {
					oldOrderBook = marketDataService.getOrderBook(cP);
					this.orderBook = oldOrderBook;
					orderBookUpdate = oldOrderBook;
				}
			} catch (Exception e) {
				try {
					printErrorAndWait(e);
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		}

		this.updDur = -1.0;
		this.latency = -1.0;
		lastNbTrades = (tradesUpdate==null)?-1:tradesUpdate.size();
		
		long tBeforeUpd;
		
		while (true) {
			try {
				
				if (enableServices[0]) {
					if (tradesUpdate.size()>0) {
						bufferedWriter.write(xchangeTools.tradeListToString(tradesUpdate));
						bufferedWriter.newLine();
						bufferedWriter.flush();
					}
				}
				if (enableServices[1]) {
					if (orderBookUpdate.getAsks().size()+orderBookUpdate.getBids().size()>0) {
						bufferedWriter.write(xchangeTools.obToString(orderBookUpdate));
						bufferedWriter.newLine();
						bufferedWriter.flush();
					}
				}
				
				// Waiting enough for not being banned
				try {
					Thread.sleep((long) (2200*requestWaitSeconds));
				} catch (InterruptedException e1) {
					break;
				}
				
				tBeforeUpd = new Date().getTime();
				
				// Trades updates
				if (enableServices[0]) {
					this.trades = getTrade(marketDataService, cP);
					tradesUpdate = xchangeTools.computeTradesUpdates(this.trades, oldTrades);
					oldTrades = this.trades;
				}

				// Orderbook updates
				if (enableServices[1]) {
					this.orderBook = marketDataService.getOrderBook(cP);
					orderBookUpdate = xchangeTools.computeObUpdates(orderBook, oldOrderBook);
					oldOrderBook = orderBook;
				}
				
				this.latency = (new Date().getTime()-tBeforeUpd)/1000.0;
				
				lastNbTrades = (tradesUpdate==null)?-1:tradesUpdate.size();
				
				newTs = new Date().getTime();
				this.updDur = (newTs - lastTs)/1000.0;
				lastTs = newTs;

			} catch (Exception e) {
				if (e.getClass() == new InterruptedException().getClass()){
					break;
				}
				try {
					printErrorAndWait(e);
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					break;
				}
			}
		}
	}
	Recorder(String outputDir, Integer ind2record) throws Exception {
		this.outputDir = outputDir;
		this.settings  = watchList[ind2record];
		// Use the factory to get chosen exchange API using default settings
		marketName =  this.settings.marketToRecord;
		String[] marketNameSplit = marketName.split("\\.");
		shortMarketName = marketNameSplit[3];
	}	

	// missing : 
	// The rock trading company? Exmo? BTCmarkets, Vicurex ne marche pas?
	// no API : LocalBitcoin, Bitcoin.de, zyado
	// references :
	// http://bitcoincharts.com/markets/
	// https://bitcoinity.org/markets/list?currency=ALL
	// à regarder : 
	// https://www.cryptocoincharts.info/markets/info
	// http://www.bitcoinwatch.com/

	// 
	public final static RecorderConfig[] watchList = {
		
	//	new RecorderConfig(BTCChinaExchange.class.getName(),       CurrencyPair.BTC_CNY, 0.5, new Boolean[] {true, true}),

	//	new RecorderConfig(OkCoinExchange.class.getName(),         CurrencyPair.BTC_CNY, 0.5, new Boolean[] {true, true}),
	//	new RecorderConfig(OkCoinExchange.class.getName(),         CurrencyPair.BTC_USD, 0.5, new Boolean[] {true, true}),

		// new RecorderConfig(HuobiExchange.class.getName(),          CurrencyPair.BTC_CNY, 1.0, new Boolean[] {true, true}),

	//	new RecorderConfig(BitfinexExchange.class.getName(),       CurrencyPair.BTC_USD, 1.0, new Boolean[] {true, true}),

		// new RecorderConfig(CoinbaseExchange.class.getName(),       CurrencyPair.BTC_USD, 1.0, new Boolean[] {true, false}),
		// new RecorderConfig(CoinbaseExchange.class.getName(),       CurrencyPair.BTC_EUR, 1.0, new Boolean[] {true, false}),
		// new RecorderConfig(CoinbaseExchange.class.getName(),       CurrencyPair.BTC_GBP, 1.0, new Boolean[] {true, false}),


		// new RecorderConfig(LakeBTCExchange.class.getName(),        CurrencyPair.BTC_USD, 1.0, new Boolean[] {true, false}),
		// new RecorderConfig(LakeBTCExchange.class.getName(),        CurrencyPair.BTC_CNY, 1.0, new Boolean[] {true, false}),

		// new RecorderConfig(ANXExchange.class.getName(),            CurrencyPair.BTC_AUD, 1.0, new Boolean[] {true, false}),
		// new RecorderConfig(ANXExchange.class.getName(),            CurrencyPair.BTC_CAD, 1.0, new Boolean[] {true, false}),
		// new RecorderConfig(ANXExchange.class.getName(),            CurrencyPair.BTC_CNY, 1.0, new Boolean[] {true, false}),
		// new RecorderConfig(ANXExchange.class.getName(),            CurrencyPair.BTC_CHF, 1.0, new Boolean[] {true, false}),
		// new RecorderConfig(ANXExchange.class.getName(),            CurrencyPair.BTC_EUR, 1.0, new Boolean[] {true, false}),
		// new RecorderConfig(ANXExchange.class.getName(),            CurrencyPair.BTC_GBP, 1.0, new Boolean[] {true, false}),
		// new RecorderConfig(ANXExchange.class.getName(),            CurrencyPair.BTC_HKD, 1.0, new Boolean[] {true, false}),
		// new RecorderConfig(ANXExchange.class.getName(),            CurrencyPair.BTC_JPY, 1.0, new Boolean[] {true, false}),
		// new RecorderConfig(ANXExchange.class.getName(),            CurrencyPair.BTC_NZD, 1.0, new Boolean[] {true, false}),
		// new RecorderConfig(ANXExchange.class.getName(),            CurrencyPair.BTC_SGD, 1.0, new Boolean[] {true, false}),
		// new RecorderConfig(ANXExchange.class.getName(),            CurrencyPair.BTC_USD, 1.0, new Boolean[] {true, false}),


/*		new RecorderConfig(BTCEExchange.class.getName(),           CurrencyPair.BTC_USD, 1.0, new Boolean[] {true, true}),
*/		new RecorderConfig(BTCEExchange.class.getName(),           CurrencyPair.BTC_EUR, 1.0, new Boolean[] {false, true}),
		/* new RecorderConfig(BTCEExchange.class.getName(),           CurrencyPair.BTC_RUR, 1.0, new Boolean[] {true, true}),
		*/
		
		// new RecorderConfig(BitstampExchange.class.getName(),       CurrencyPair.BTC_USD, 1.0, new Boolean[] {true, true}),

		new RecorderConfig(KrakenExchange.class.getName(),         CurrencyPair.BTC_EUR, 2.0, new Boolean[] {false, true}),
	//	new RecorderConfig(KrakenExchange.class.getName(),         CurrencyPair.BTC_USD, 4.0, new Boolean[] {true, true}),

		// new RecorderConfig(ItBitExchange.class.getName(),          CurrencyPair.BTC_USD, 1.0, new Boolean[] {false, true}),
		// new RecorderConfig(ItBitExchange.class.getName(),          CurrencyPair.BTC_SGD, 1.0, new Boolean[] {false, true}),
		new RecorderConfig(ItBitExchange.class.getName(),          CurrencyPair.BTC_EUR, 1.0, new Boolean[] {false, true}),


		// new RecorderConfig(CoinsetterExchange.class.getName(),     CurrencyPair.BTC_USD, 1.0, new Boolean[] {true, false}),

		/*
		new RecorderConfig(BitcurexExchange.class.getName(),       CurrencyPair.BTC_PLN, 1.0, new Boolean[] {true, true}),
*/		new RecorderConfig(BitcurexExchange.class.getName(),       CurrencyPair.BTC_EUR, 1.0, new Boolean[] {false, true}),
/*		new RecorderConfig(BitcurexExchange.class.getName(),       CurrencyPair.BTC_USD, 1.0, new Boolean[] {true, true}),
*/		
		
		// new RecorderConfig(HitbtcExchange.class.getName(),         CurrencyPair.BTC_USD, 1.0, new Boolean[] {true, true}),
		new RecorderConfig(HitbtcExchange.class.getName(),         CurrencyPair.BTC_EUR, 1.0, new Boolean[] {false, true}),
		
		
		/*new RecorderConfig(MercadoBitcoinExchange.class.getName(), CurrencyPair.BTC_BRL, 1.0, new Boolean[] {true, true}),
		*/
		
		// new RecorderConfig(CleverCoinExchange.class.getName(),     CurrencyPair.BTC_EUR, 1.0, new Boolean[] {true, true}),

		/* new RecorderConfig(VirtExExchange.class.getName(),         CurrencyPair.BTC_CAD, 1.0, new Boolean[] {true, true}),
		*/
		
		new RecorderConfig(BTCCentralExchange.class.getName(),     CurrencyPair.BTC_EUR, 1.0, new Boolean[] {false, true}),

		// new RecorderConfig(CoinfloorExchange.class.getName(),      CurrencyPair.BTC_GBP, 1.0, new Boolean[] {true, false}),

		/* new RecorderConfig(BitbayExchange.class.getName(),         CurrencyPair.BTC_PLN, 1.0, new Boolean[] {true, true}),
		new RecorderConfig(BitbayExchange.class.getName(),         CurrencyPair.BTC_EUR, 1.0, new Boolean[] {true, true}),
		*/
		
		//new RecorderConfig(VircurexExchange.class.getName(),         CurrencyPair.BTC_EUR),

	};

	public final static void main(String[] args) throws Exception {
		int ind2record = 0;
		String outputDir = "/home/tonigor/btcdata";
		if (args.length > 0){
			outputDir = args[0];
			ind2record = Integer.parseInt(args[1]);
		}
		Recorder r = new Recorder(outputDir, ind2record);
		r.run();
		// raw((BitstampMarketDataServiceRaw) marketDataService);
	}
	public double getLatency() {
		return this.latency;
	}
	public double getUpdDur() {
		return this.updDur;
	}
	public OrderBook getOrdebook() {
		return this.orderBook;
	}
	public Trades getTrades() {
		return this.trades;
	}
}
