package _recorder._recorder;

import java.awt.GridLayout;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableModel;

import com.xeiam.xchange.dto.marketdata.OrderBook;
import com.xeiam.xchange.dto.marketdata.Trade;
import com.xeiam.xchange.dto.marketdata.Trades;
import com.xeiam.xchange.dto.trade.LimitOrder;
import com.xeiam.xchart.Chart;
import com.xeiam.xchart.Series;
import com.xeiam.xchart.SeriesMarker;
import com.xeiam.xchart.StyleManager.ChartType;
import com.xeiam.xchart.XChartPanel;

public final class xchangeTools {
	public final static void printTonsOfExcl(){
		System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
		System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
		System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
		System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
		System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
		System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
		System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
		System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
	}
	public final static void ob2jtable(OrderBook ob, String name) {
		JFrame jf = new JFrame();
		jf.setSize(1200, 200);
		jf.setLocation(0, 0);
		jf.setTitle(name);
		
		
		Object[][] data = new Object[20][];
		double accAsk = 0.0;
		double accBid = 0.0;
		for (int i = 0; i < data.length; i++) {
			accAsk += ob.getAsks().get(i).getTradableAmount().doubleValue();
			accBid += ob.getBids().get(i).getTradableAmount().doubleValue();
			data[i] = new Object[]{accBid, ob.getBids().get(i).getTradableAmount(), ob.getBids().get(i).getLimitPrice(), 
					ob.getAsks().get(i).getLimitPrice(), ob.getAsks().get(i).getTradableAmount(), accAsk};
		}
		String[] titles = {"Cumulated Bid", "amount", "Bid Price", "Cumulated Ask", "amount", "Ask Price"};
		JTable jt = new JTable(data, titles);
		jf.getContentPane().add(new JScrollPane(jt));
		jf.setVisible(true);
	}
	public final static void plotOB(OrderBook ob, String name) {
		// Create Chart
		Chart chart = new Chart(800, 500);

		// Customize Chart
		chart.setChartTitle(name);
		chart.setYAxisTitle(ob.getBids().get(0).getCurrencyPair().baseSymbol);
		chart.setXAxisTitle(ob.getBids().get(0).getCurrencyPair().counterSymbol);
		chart.getStyleManager().setChartType(ChartType.Area);

		// BIDS
		List<Number> xData = new ArrayList<Number>();
		List<Number> yData = new ArrayList<Number>();
		BigDecimal accumulatedBidUnits = new BigDecimal("0");
		for (LimitOrder limitOrder : ob.getBids()) {
			if (limitOrder.getLimitPrice().doubleValue() > 10) {
				xData.add(limitOrder.getLimitPrice());
				accumulatedBidUnits = accumulatedBidUnits.add(limitOrder.getTradableAmount());
				yData.add(accumulatedBidUnits);
			}
		}
		Collections.reverse(xData);
		Collections.reverse(yData);

		// Bids Series
		Series series = chart.addSeries("bids", xData, yData);
		series.setMarker(SeriesMarker.NONE);

		// ASKS
		xData = new ArrayList<Number>();
		yData = new ArrayList<Number>();
		BigDecimal accumulatedAskUnits = new BigDecimal("0");
		for (LimitOrder limitOrder : ob.getAsks()) {
			if (limitOrder.getLimitPrice().doubleValue() < 1000) {
				xData.add(limitOrder.getLimitPrice());
				accumulatedAskUnits = accumulatedAskUnits.add(limitOrder.getTradableAmount());
				yData.add(accumulatedAskUnits);
			}
		}

		// Asks Series
		series = chart.addSeries("asks", xData, yData);
		series.setMarker(SeriesMarker.NONE);


		new xchangeTools.SwingWrapper(chart).displayChart();
	}
	public final static class OBchart extends Thread{
		OrderBook ob;
		public OBchart(OrderBook ob) {
			this.ob = ob;
		}
		public void run() {

		}
	}
	public static String getExceptionString(Exception e){
		String s = "\t" + e.toString() + "\n";
		StackTraceElement[] elements = e.getStackTrace();
		for(int i=0; i<elements.length; i++) {
			s += "\t\t" + elements[i] + "\n";
		}
		return s;	
	}
	private static String tsToString (Date d){
		String its = null;
		if (d!=null){
			its = Long.toString(d.getTime());
		}
		return its;
	}
	public static String obToString(OrderBook ob){
		Date date = new Date();
		return "Orderbook ts=" + date.getTime() + " its=" + tsToString(ob.getTimeStamp()) 
				+ " [asks=" + oneSideObToString(ob.getAsks()) + "]"
				+ " [bids=" + oneSideObToString(ob.getBids()) + "]";
	}
	private static String oneSideObToString(List<LimitOrder> oSob){
		String s = "[";
		for (LimitOrder o : oSob) {
			s += "[" + o.getLimitPrice() + ", " + o.getTradableAmount() + "], ";
		}
		s += "]";
		return s;
	}
	public static String tradeListToString(List<Trade> tradeList){
		Date date = new Date();
		String s = "Trades ts=" + date.getTime();
		for (Trade trade : tradeList) {
			s += " [price=" + trade.getPrice() + " qty=" + trade.getTradableAmount()
					+ " id=" + trade.getId() + " ts=" + tsToString(trade.getTimestamp())
					+ " type=" + trade.getType() + "],";
		}
		return s;
	}
	public static List<Trade> computeTradesUpdates(Trades trades, Trades oldTrades) throws Exception{
		List<Trade> tradesUpdate = new ArrayList<Trade>();
		tradesUpdate.addAll(trades.getTrades());
		List<Trade> oldTradesList = oldTrades.getTrades();
		Trade tradeU;
		for (Trade trade : oldTradesList) {
			for (int i = 0; i < tradesUpdate.size(); i++) {
				tradeU = tradesUpdate.get(i);
				if (trade.getTimestamp().equals(tradeU.getTimestamp()) 
						&& trade.getPrice().equals(tradeU.getPrice()) 
						&& trade.getTradableAmount().equals(tradeU.getTradableAmount()) 
						&& trade.getId().equals(tradeU.getId())
						&& (trade.getType() == null || trade.getType().equals(tradeU.getType())) ){
					tradesUpdate.remove(i);
					break;
				} 
			}
		}
		return tradesUpdate;
	}
	public static OrderBook computeObUpdates(OrderBook orderbook, OrderBook old0rderBook) {
		return new OrderBook(orderbook.getTimeStamp(), 
				computeObUpdatesForOneSide(1, orderbook.getAsks(), old0rderBook.getAsks()), 
				computeObUpdatesForOneSide(-1, orderbook.getBids(), old0rderBook.getBids()));
	}
	private static List<LimitOrder> computeObUpdatesForOneSide(int correctForSide, List<LimitOrder> orderbookOneSide, List<LimitOrder> old0rderbookOneSide) {
		List<LimitOrder> oneSideUpdates = new ArrayList<LimitOrder>();
		int oInd = 0;
		int ooInd = 0;
		int c;
		LimitOrder o = null;
		LimitOrder oo = null;
		for (int i = 0; i < Math.max(orderbookOneSide.size(), old0rderbookOneSide.size()); i++) {
			if (oInd>=orderbookOneSide.size()) {
				for (int j = ooInd; j < old0rderbookOneSide.size(); j++) {
					oo = old0rderbookOneSide.get(j);
					oneSideUpdates.add(new LimitOrder(oo.getType(), new BigDecimal(0.0), oo.getCurrencyPair(), oo.getId(), oo.getTimestamp(), oo.getLimitPrice()));
				}
				break;
			} else {
				o = orderbookOneSide.get(oInd);
			}
			if (ooInd>=old0rderbookOneSide.size()) {
				oneSideUpdates.addAll(orderbookOneSide.subList(oInd, orderbookOneSide.size()));
				break;
			} else {
				oo = old0rderbookOneSide.get(ooInd);
			}
			c = correctForSide * o.getLimitPrice().compareTo(oo.getLimitPrice());
			if (c>0) {
				oneSideUpdates.add(new LimitOrder(oo.getType(), new BigDecimal(0.0), oo.getCurrencyPair(), oo.getId(), oo.getTimestamp(), oo.getLimitPrice()));
				ooInd++;
			} else if (c<0) {
				oneSideUpdates.add(o);
				oInd++;
			} else {
				if (!o.getTradableAmount().equals(oo.getTradableAmount())) {
					oneSideUpdates.add(o);
				}
				oInd++;
				ooInd++;
			}
		}
		return oneSideUpdates;
	}
	public final static class SwingWrapper {

		private String windowTitle = "XChart";

		private List<Chart> charts = new ArrayList<Chart>();
		private int numRows;
		private int numColumns;

		/**
		 * Constructor
		 * 
		 * @param chart
		 */
		public SwingWrapper(Chart chart) {

			this.charts.add(chart);
		}

		/**
		 * Constructor - The number of rows and columns will be calculated automatically Constructor
		 * 
		 * @param charts
		 */
		public SwingWrapper(List<Chart> charts) {

			this.charts = charts;

			this.numRows = (int) (Math.sqrt(charts.size()) + .5);
			this.numColumns = (int) ((double) charts.size() / this.numRows + 1);
		}

		/**
		 * Constructor
		 * 
		 * @param charts
		 * @param numRows - the number of rows
		 * @param numColumns - the number of columns
		 */
		public SwingWrapper(List<Chart> charts, int numRows, int numColumns) {

			this.charts = charts;
			this.numRows = numRows;
			this.numColumns = numColumns;
		}

		/**
		 * Display the chart in a Swing JFrame
		 * 
		 * @param windowTitle the title of the window
		 */
		public JFrame displayChart(String windowTitle) {

			this.windowTitle = windowTitle;

			return displayChart();
		}

		/**
		 * Display the chart in a Swing JFrame
		 */
		public JFrame displayChart() {

			// Create and set up the window.
			final JFrame frame = new JFrame(windowTitle);

			// Schedule a job for the event-dispatching thread:
			// creating and showing this application's GUI.
			javax.swing.SwingUtilities.invokeLater(new Runnable() {

				@Override
				public void run() {

					frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
					JPanel chartPanel = new XChartPanel(charts.get(0));
					frame.add(chartPanel);

					// Display the window.
					frame.pack();
					frame.setVisible(true);
				}
			});

			return frame;
		}

		/**
		 * Display the charts in a Swing JFrame
		 * 
		 * @param windowTitle the title of the window
		 * @return the JFrame
		 */
		public JFrame displayChartMatrix(String windowTitle) {

			this.windowTitle = windowTitle;

			return displayChartMatrix();
		}

		/**
		 * Display the chart in a Swing JFrame
		 */
		public JFrame displayChartMatrix() {

			// Create and set up the window.
			final JFrame frame = new JFrame(windowTitle);

			// Schedule a job for the event-dispatching thread:
			// creating and showing this application's GUI.
			javax.swing.SwingUtilities.invokeLater(new Runnable() {

				@Override
				public void run() {

					frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
					frame.getContentPane().setLayout(new GridLayout(numRows, numColumns));

					for (Chart chart : charts) {
						if (chart != null) {
							JPanel chartPanel = new XChartPanel(chart);
							frame.add(chartPanel);
						}
						else {
							JPanel chartPanel = new JPanel();
							frame.getContentPane().add(chartPanel);
						}

					}

					// Display the window.
					frame.pack();
					frame.setVisible(true);
				}
			});

			return frame;
		}

	}
}
