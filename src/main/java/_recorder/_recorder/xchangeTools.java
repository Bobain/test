package _recorder._recorder;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.xeiam.xchange.dto.marketdata.OrderBook;
import com.xeiam.xchange.dto.marketdata.Trade;
import com.xeiam.xchange.dto.marketdata.Trades;
import com.xeiam.xchange.dto.trade.LimitOrder;

public final class xchangeTools {
	public static String obToString(OrderBook ob){
		Date date = new Date();
		return "Orderbook ts=" + date.getTime() + " its=" + ob.getTimeStamp().getTime() 
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
					+ " id=" + trade.getId() + " ts=" + trade.getTimestamp().getTime()
					+ " type=" + trade.getType() + "],";
		}
		return s;
	}
	public static List<Trade> computeTradesUpdates(Trades trades, Trades oldTrades) throws Exception{
		if (!trades.getTradeSortType().equals(com.xeiam.xchange.dto.marketdata.Trades.TradeSortType.SortByID)) {
			throw new Exception("Only id sorted trades is implemented");
		}
		List<Trade> tradesUpdate = trades.getTrades();
		String lastId = Long.toString(oldTrades.getlastID());
		int i;
		for (i = 0; i < tradesUpdate.size(); i++) {
			if (tradesUpdate.get(i).getId().equals(lastId)) {
				i++;
				break;
			}
		}
		tradesUpdate.subList(0, i).clear();
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
			c = o.getLimitPrice().compareTo(oo.getLimitPrice());
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
}
