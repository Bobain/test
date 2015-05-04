package _recorder._recorder;


import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.xeiam.xchange.Exchange;
import com.xeiam.xchange.ExchangeFactory;
import com.xeiam.xchange.anx.v2.ANXExchange;
import com.xeiam.xchange.bitbay.BitbayExchange;
import com.xeiam.xchange.bitcurex.BitcurexExchange;
import com.xeiam.xchange.bitfinex.v1.BitfinexExchange;
import com.xeiam.xchange.bitstamp.BitstampExchange;
import com.xeiam.xchange.btccentral.BTCCentralExchange;
import com.xeiam.xchange.btcchina.BTCChinaExchange;
import com.xeiam.xchange.btce.v3.BTCEExchange;
import com.xeiam.xchange.clevercoin.CleverCoinExchange;
import com.xeiam.xchange.coinbase.CoinbaseExchange;
import com.xeiam.xchange.coinfloor.CoinfloorExchange;
import com.xeiam.xchange.coinsetter.CoinsetterExchange;
import com.xeiam.xchange.currency.CurrencyPair;
import com.xeiam.xchange.dto.marketdata.OrderBook;
import com.xeiam.xchange.dto.marketdata.Trade;
import com.xeiam.xchange.dto.marketdata.Trades;
import com.xeiam.xchange.hitbtc.HitbtcExchange;
import com.xeiam.xchange.huobi.HuobiExchange;
import com.xeiam.xchange.itbit.v1.ItBitExchange;
import com.xeiam.xchange.kraken.KrakenExchange;
import com.xeiam.xchange.lakebtc.LakeBTCExchange;
import com.xeiam.xchange.mercadobitcoin.MercadoBitcoinExchange;
import com.xeiam.xchange.okcoin.OkCoinExchange;
import com.xeiam.xchange.service.polling.marketdata.PollingMarketDataService;
import com.xeiam.xchange.virtex.v2.VirtExExchange;


/**
 * Recording a specified orderbook activity using rest API
 */
public final class Recorder {
	public final static class RecorderConfig {
		public final String marketToRecord;
		public final CurrencyPair currencyPairToRecord;
		public final double requestWaitSeconds;
		public RecorderConfig(String mtr, CurrencyPair cptr, Double rws) {
			this.marketToRecord = mtr;
			this.currencyPairToRecord = cptr;
			this.requestWaitSeconds = rws;
		}
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
		new RecorderConfig(BTCChinaExchange.class.getName(),       CurrencyPair.BTC_CNY, 0.5),

		new RecorderConfig(OkCoinExchange.class.getName(),         CurrencyPair.BTC_CNY, 0.5),
		new RecorderConfig(OkCoinExchange.class.getName(),         CurrencyPair.BTC_USD, 0.5),

		new RecorderConfig(HuobiExchange.class.getName(),          CurrencyPair.BTC_CNY, 1.0),

		new RecorderConfig(BitfinexExchange.class.getName(),       CurrencyPair.BTC_USD, 1.0),

		new RecorderConfig(CoinbaseExchange.class.getName(),       CurrencyPair.BTC_USD, 1.0),
		new RecorderConfig(CoinbaseExchange.class.getName(),       CurrencyPair.BTC_EUR, 1.0),
		new RecorderConfig(CoinbaseExchange.class.getName(),       CurrencyPair.BTC_GBP, 1.0),

		new RecorderConfig(LakeBTCExchange.class.getName(),        CurrencyPair.BTC_USD, 1.0),
		new RecorderConfig(LakeBTCExchange.class.getName(),        CurrencyPair.BTC_CNY, 1.0),

		new RecorderConfig(ANXExchange.class.getName(),            CurrencyPair.BTC_AUD, 1.0),
		new RecorderConfig(ANXExchange.class.getName(),            CurrencyPair.BTC_CAD, 1.0),
		new RecorderConfig(ANXExchange.class.getName(),            CurrencyPair.BTC_CNY, 1.0),
		new RecorderConfig(ANXExchange.class.getName(),            CurrencyPair.BTC_CHF, 1.0),
		new RecorderConfig(ANXExchange.class.getName(),            CurrencyPair.BTC_EUR, 1.0),
		new RecorderConfig(ANXExchange.class.getName(),            CurrencyPair.BTC_GBP, 1.0),
		new RecorderConfig(ANXExchange.class.getName(),            CurrencyPair.BTC_HKD, 1.0),
		new RecorderConfig(ANXExchange.class.getName(),            CurrencyPair.BTC_JPY, 1.0),
		new RecorderConfig(ANXExchange.class.getName(),            CurrencyPair.BTC_NZD, 1.0),
		new RecorderConfig(ANXExchange.class.getName(),            CurrencyPair.BTC_SGD, 1.0),
		new RecorderConfig(ANXExchange.class.getName(),            CurrencyPair.BTC_USD, 1.0),

		new RecorderConfig(BTCEExchange.class.getName(),           CurrencyPair.BTC_USD, 1.0),
		new RecorderConfig(BTCEExchange.class.getName(),           CurrencyPair.BTC_EUR, 1.0),
		new RecorderConfig(BTCEExchange.class.getName(),           CurrencyPair.BTC_RUR, 1.0),

		new RecorderConfig(BitstampExchange.class.getName(),       CurrencyPair.BTC_USD, 1.0),

		new RecorderConfig(KrakenExchange.class.getName(),         CurrencyPair.BTC_EUR, 1.0),
		new RecorderConfig(KrakenExchange.class.getName(),         CurrencyPair.BTC_USD, 1.0),
		new RecorderConfig(KrakenExchange.class.getName(),         CurrencyPair.BTC_KRW, 1.0),

		new RecorderConfig(ItBitExchange.class.getName(),          CurrencyPair.BTC_USD, 1.0),
		new RecorderConfig(ItBitExchange.class.getName(),          CurrencyPair.BTC_SGD, 1.0),
		new RecorderConfig(ItBitExchange.class.getName(),          CurrencyPair.BTC_EUR, 1.0),

		new RecorderConfig(CoinsetterExchange.class.getName(),     CurrencyPair.BTC_USD, 1.0),

		new RecorderConfig(BitcurexExchange.class.getName(),       CurrencyPair.BTC_PLN, 1.0),
		new RecorderConfig(BitcurexExchange.class.getName(),       CurrencyPair.BTC_EUR, 1.0),
		new RecorderConfig(BitcurexExchange.class.getName(),       CurrencyPair.BTC_USD, 1.0),

		new RecorderConfig(HitbtcExchange.class.getName(),         CurrencyPair.BTC_USD, 1.0),
		new RecorderConfig(HitbtcExchange.class.getName(),         CurrencyPair.BTC_EUR, 1.0),

		new RecorderConfig(MercadoBitcoinExchange.class.getName(), CurrencyPair.BTC_BRL, 1.0),

		new RecorderConfig(CleverCoinExchange.class.getName(),     CurrencyPair.BTC_EUR, 1.0),

		new RecorderConfig(VirtExExchange.class.getName(),         CurrencyPair.BTC_CAD, 1.0),

		new RecorderConfig(BTCCentralExchange.class.getName(),     CurrencyPair.BTC_EUR, 1.0),

		new RecorderConfig(CoinfloorExchange.class.getName(),      CurrencyPair.BTC_GBP, 1.0),

		new RecorderConfig(BitbayExchange.class.getName(),         CurrencyPair.BTC_PLN, 1.0),
		new RecorderConfig(BitbayExchange.class.getName(),         CurrencyPair.BTC_EUR, 1.0),
		//new RecorderConfig(VircurexExchange.class.getName(),         CurrencyPair.BTC_EUR),

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

		restAPIrecorder(outputDir, marketDataService, marketName, 
				watchList[ind2record].currencyPairToRecord, 
				watchList[ind2record].requestWaitSeconds);
		// raw((BitstampMarketDataServiceRaw) marketDataService);

	}

	private final static void restAPIrecorder(String outputDir, PollingMarketDataService marketDataService, String marketName, CurrencyPair cP, double requestWaitSeconds) throws Exception {
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
		Trades trades;
		Trades oldTrades;
		java.util.List<Trade> tradesUpdate;
		// orderbooks variables
		OrderBook oldOrderBook;
		OrderBook orderBook;
		OrderBook orderBookUpdate;

		// Ouput file management
		String dataOutputDir = outputDir + "/" + cP.toString().replaceAll("/", "_") + "/" + shortMarketName; 
		if(!new File(dataOutputDir).exists()) {
			new File(dataOutputDir).mkdirs();
		}
		FileWriter fileWriter = new FileWriter(dataOutputDir + "/" + dateFormat.format(date)  + ".obat");
		BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
		
		try {
			trades = marketDataService.getTrades(cP);
			oldTrades = trades;
			tradesUpdate = trades.getTrades();

			oldOrderBook = marketDataService.getOrderBook(cP);
			orderBook = oldOrderBook;
			orderBookUpdate = oldOrderBook;

			
		} catch (Exception e1) {
			if (fileWriterError == null) {
				fileWriterError = new FileWriter(errorFilename);
			}
			fileWriterError.write("Error Timestamp=" + new Date().getTime() + "\n" + e1.toString() + "\n");
			e1.printStackTrace();
			fileWriterError.close();
			bufferedWriter.close();
			return;
		}

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
				Thread.sleep((long) (2200*requestWaitSeconds));

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
				fileWriterError.write("Error Timestamp=" + new Date().getTime() + "\n" + e.toString() + "\n");
				e.printStackTrace();
				//fileWriterError.close();
				//bufferedWriter.close();
				// Waiting enough for not being banned : An error will most probably occur when requesting data
				Thread.sleep((long) (2200*requestWaitSeconds));
			}
		}
	}
}
