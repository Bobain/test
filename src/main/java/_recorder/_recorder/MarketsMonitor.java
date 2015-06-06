package _recorder._recorder;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.swing.AbstractCellEditor;
import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

import com.xeiam.xchange.dto.marketdata.OrderBook;
import com.xeiam.xchange.dto.trade.LimitOrder;

import _recorder._recorder.Recorder.RecorderConfig;

public final class MarketsMonitor {
	public static class MonitorMainWindow extends JFrame {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		private JTable jtable;
		private Object[][] data;
		private List<Recorder> threads;
		String[] titles = {"Market Name", "Currency Pair", "data feed working", 
				"has error", "Thread alive", "Draw orderbook", "update duration (sec)", 
				"Latency (sec)", "Best Bid", "Best Ask"};
		private ZModel zModel;

		public MonitorMainWindow(String outputDir){
			this.setLocationRelativeTo(null);
			this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			this.setTitle("Orderbook threads monitor");
			this.setSize(1200, 1200);
			this.setLocation(0, 0);


			threads = new ArrayList<Recorder>();
			for (int i = 0; i < Recorder.watchList.length; i++) {
				try {
					threads.add(new Recorder(outputDir, i));
					threads.get(i).start();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			this.createContent();
		}


		public class ButtonRenderer extends JButton implements TableCellRenderer{

			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean isFocus, int row, int col) {
				// On écrit dans le bouton avec la valeur de la cellule
				setText((value != null) ? value.toString() : "");
				// On retourne notre bouton
				return this;
			}
		}
		private static class ValueRenderer extends JCheckBox
		implements TableCellRenderer {
			@Override
			public Component getTableCellRendererComponent(
					JTable table, Object value, boolean isSelected,
					boolean hasFocus, int row, int col) {

				if (isSelected) {
					setForeground(table.getSelectionForeground());
					setBackground(table.getSelectionBackground());
				} else {
					setForeground(table.getForeground());
					setBackground(table.getBackground());
				}
				return this;
			}
		}



		private class ValueEditor extends AbstractCellEditor 
		implements TableCellEditor, ItemListener {

			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;
			private ValueRenderer vr = new ValueRenderer();

			public ValueEditor() {
				vr.addItemListener(this);
			}

			@Override
			public Object getCellEditorValue() {
				return vr.isSelected();
			}

			@Override
			public Component getTableCellEditorComponent(JTable table,
					Object value, boolean isSelected, int row, int col) {
				vr.setSelected(isSelected);
				if (isSelected){
					if (!threads.get(row).isAlive() || threads.get(row).isInterrupted()){
						try {
							threads.add(row, new Recorder(threads.get(row).outputDir, row));
						} catch (Exception e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
						threads.remove(row+1);
						threads.get(row).start();
					}
				} else {
					threads.get(row).interrupt();
					try {
						threads.get(row).join();
					} catch (InterruptedException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				}
				return vr;
			}

			@Override
			public void itemStateChanged(ItemEvent e) {
				this.fireEditingStopped();
			}
		}

		public class ButtonEditor extends DefaultCellEditor {

			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;
			protected JButton button;
			private ButtonListener bListener;
			public ButtonEditor(JCheckBox checkBox, List<Recorder> ts) {
				//Par défaut, ce type d'objet travaille avec un JCheckBox
				super(checkBox);
				bListener = new ButtonListener();
				bListener.setThreads(ts);
				//On crée à nouveau notre bouton
				button = new JButton();
				button.setOpaque(true);
				//On lui attribue un listener
				button.addActionListener(bListener);
			}

			public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column){
				//On définit le numéro de ligne à notre listener
				bListener.setRow(row);
				//Idem pour le numéro de colonne
				bListener.setColumn(column);
				//On passe aussi en paramètre le tableau pour des actions potentielles
				bListener.setTable(table);
				//On réaffecte le libellé au bouton
				button.setText( (value ==null) ? "" : value.toString() );
				//On renvoie le bouton
				return button;
			}

			class ButtonListener implements ActionListener {

				private int column, row;
				private JButton button;
				private List<Recorder> threads;

				public void setThreads(List<Recorder> ts) {
					this.threads = ts;
				}
				public void setColumn(int col){this.column = col;}
				public void setRow(int row){this.row = row;}
				public void setTable(JTable table){}
				public JButton getButton(){return this.button;}

				@Override
				public void actionPerformed(ActionEvent event) {
					if (this.column == 5){
						this.threads.get(this.row).plotOb();
					} else if (this.column == 3 && this.threads.get(this.row).fileWriterError != null) {
						File file = new File(this.threads.get(this.row).errorFilename);
						BufferedReader reader = null;
						try {
							reader = new BufferedReader(new FileReader(file));
							String text = null;
							String savetext=null;

							while ((text = reader.readLine()) != null) {
								savetext += text;
							}
							JFrame jf = new JFrame(this.threads.get(this.row).errorFilename);
							jf.setSize(1200, 1000);
							jf.setLocation(0, 0);
							JTextArea textarea = new JTextArea();
							textarea.setText(savetext);
							jf.add(textarea);
							textarea.setVisible(true);
							jf.setVisible(true);
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}

			}
		}

		private void createContent(){
			//Données de notre tableau
			data = new Object[Recorder.watchList.length][4];
			RecorderConfig rc;
			for (int i = 0; i < Recorder.watchList.length; i++) {
				rc = Recorder.watchList[i];
				data[i] = new Object[]{threads.get(i).shortMarketName, 
						rc.currencyPairToRecord, "Trades: NA OB: NA",
						"No", true, "DrawOB", -1.0, -1.0, -1.0, -1.0};
			}

			//Notre modèle d'affichage spécifique destiné à pallier
			//les bugs d'affichage !
			this.zModel = new ZModel(data, titles, threads);

			this.jtable = new JTable(zModel) {

				/**
				 * 
				 */
				private static final long serialVersionUID = 1L;
				/**
				 * 

				private static final long serialVersionUID = 1L;

				@Override
				public Component prepareRenderer(TableCellRenderer renderer, int row, int col) {
					Component comp = super.prepareRenderer(renderer, row, col);
					Object value = getModel().getValueAt(row, col);
					if (3 == row) {
						if (value.equals("Yes, click so see it")) {
							comp.setBackground(Color.red);
						} else {
							comp.setBackground(Color.white);
						}
					}
					return comp;
				}   */ 
			};
			this.jtable.setRowHeight(30);

			this.jtable.getColumn("Draw orderbook").setCellRenderer(new ButtonRenderer());
			this.jtable.getColumn("Draw orderbook").setCellEditor(new ButtonEditor(new JCheckBox(), this.threads));

			this.jtable.getColumn("Thread alive").setCellEditor(new ValueEditor());

			this.jtable.getColumn("has error").setCellRenderer(new ButtonRenderer());
			this.jtable.getColumn("has error").setCellEditor(new ButtonEditor(new JCheckBox(), this.threads));

			//On définit l'éditeur par défaut pour la cellule
			//en lui spécifiant quel type d'affichage prendre en compte

			// this.jtable.getColumn("Taille").setCellEditor(new DefaultCellEditor(combo));
			// DefaultTableCellRenderer dcr = new DefaultTableCellRenderer();
			// this.jtable.getColumn("Taille").setCellRenderer(dcr);

			//On définit un éditeur pour la colonne "supprimer"
			// this.jtable.getColumn("Suppression").setCellEditor(new DeleteButtonEditor(new JCheckBox()));

			//On ajoute le tableau
			// JPanel jp1 = new JPanel();
			// jp1.add();


			// JButton jb = new JButton("Plot Whole OB");
			// jb.addActionListener(ObCumPlotter(this.threads));

			// JPanel jp2 = new JPanel();
			// jp2.add(jb, BorderLayout.SOUTH);

			this.add(new JScrollPane(jtable));
			//this.add(jp2);

		}     

	}

	public final static void main(String[] args) throws InterruptedException {
		// String timeout = "30";
		String outputDir = "/home/tonigor/btcdata";
		// String jarPath = "/home/tonigor/JavaBTCproject/_recorder/target/recorder-3.0.1-SNAPSHOT.jar";
		if (args.length>0) {
			// timeout = args[0];
			outputDir = args[0];
			// jarPath = args[2];
		}

		MonitorMainWindow mmw;
		mmw = new MarketsMonitor.MonitorMainWindow(outputDir);
		mmw.setVisible(true);

		while (true){
			for (int i = 0; i < mmw.threads.size(); i++) {
				//		mmw.threads.get(i).wait();
				for (int j = 2; j < mmw.zModel.getColumnCount(); j++) {
					mmw.zModel.setValueAt(null, i, j);
				}
				//		mmw.threads.get(i).notify();
			}
			mmw.zModel.fireTableDataChanged();
			mmw.jtable.validate();
			mmw.jtable.repaint();
			
			/* Boolean readyToPlotCum = true;
			for (Recorder t: mmw.threads) {
				if (t.orderBook == null){
					readyToPlotCum = false;
					break;
				}
			}
			if (readyToPlotCum){
				OrderBook ob = new OrderBook(null, new ArrayList<LimitOrder>(), new ArrayList<LimitOrder>());
				for (Recorder t: mmw.threads) {
					ob.getAsks().addAll(t.orderBook.getAsks());
					ob.getBids().addAll(t.orderBook.getBids());
				}
				Collections.sort(ob.getAsks());
				Collections.sort(ob.getBids());
				xchangeTools.plotOB(ob, "Cumulated Orderbook");
			}*/
			
			Thread.sleep(2000);
		}
	}
	public static class ZModel extends AbstractTableModel{

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		private List<Recorder> threads;
		private Object[][] data;
		private String[] title;

		//Constructeur
		public ZModel(Object[][] data, String[] title, List<Recorder> threads){
			this.data = data;
			this.title = title;
			this.threads = threads;
		}

		//Retourne le titre de la colonne à l'indice spécifié
		public String getColumnName(int col) {
			return this.title[col];
		}

		//Retourne le nombre de colonnes
		public int getColumnCount() {
			return this.title.length;
		}

		//Retourne le nombre de lignes
		public int getRowCount() {
			return this.data.length;
		}

		//Retourne la valeur à l'emplacement spécifié
		public Object getValueAt(int row, int col) {
			return this.data[row][col];
		}

		//Définit la valeur à l'emplacement spécifié
		public void setValueAt(Object value, int row, int col) {
			Recorder r = threads.get(row);
			/*		try {
				r.wait();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			 */		if (0==col){ this.data[row][col] = r.shortMarketName;}
			 else if (1==col){this.data[row][col] = r.settings.currencyPairToRecord;}
			 else if (2==col) {this.data[row][col] = "Trades:" + r.settings.enableServices[0] + "  OB:" + r.settings.enableServices[1];}
			 else if (3==col) {this.data[row][col] = (r.fileWriterError != null)?"Yes, click to see it":"No";}
			 else if (4==col) {this.data[row][col] = r.isAlive();}
			 else if (5==col) {this.data[row][col] = "DrawOB";}
			 else if (6==col) {this.data[row][col] = r.getUpdDur();}
			 else if (7==col) {this.data[row][col] = r.getLatency();}
			 else if (8==col) {this.data[row][col] = r.orderBook==null?-1.0:r.orderBook.getBids().get(0).getLimitPrice();}
			 else if (9==col) {this.data[row][col] = r.orderBook==null?-1.0:r.orderBook.getAsks().get(0).getLimitPrice();}
			 this.fireTableCellUpdated(row, col);
			 //		r.notify();
		}

		//Retourne la classe de la donnée de la colonne
		public Class getColumnClass(int col){
			//On retourne le type de la cellule à la colonne demandée
			//On se moque de la ligne puisque les données sont les mêmes
			//On choisit donc la première ligne
			return this.data[0][col].getClass();
		}

		//Permet d'ajouter une ligne dans le tableau
		public void addRow(Object[] data){
			int indice = 0, nbRow = this.getRowCount(), nbCol = this.getColumnCount();

			Object temp[][] = this.data;
			this.data = new Object[nbRow+1][nbCol];

			for(Object[] value : temp)
				this.data[indice++] = value;


			this.data[indice] = data;
			temp = null;
			//Cette méthode permet d'avertir le tableau que les données
			//ont été modifiées, ce qui permet une mise à jour complète du tableau
			this.fireTableDataChanged();
		}


		public boolean isCellEditable(int row, int col){
			if (col < 3) {
				return false;
			} else {
				return true;
			}
		}
	}
}
