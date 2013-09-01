package com.sad301.exc;

import gnu.io.CommPort;
import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import javax.swing.ComboBoxModel;
import javax.swing.GroupLayout;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JToggleButton;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.ListDataListener;
import javax.swing.text.DefaultCaret;

public class SerialExc extends JFrame implements SerialPortEventListener {

	/**
	 * 
	 */
	private boolean isOpened;
	private byte[] buffer = new byte[1024];
	private static final long serialVersionUID = 1L;
	private InputStream in;
	private JComboBox<String> cbCommPortId;
	private JTextArea taData;
	private JToggleButton bConnect;
	private SerialPort serialPort;
	
	public static void main(String[] args) throws Exception {
		String laf = UIManager.getSystemLookAndFeelClassName();
		UIManager.setLookAndFeel(laf);
		SerialExc sx = new SerialExc();
		sx.setVisible(true);
	}
	
	public SerialExc() {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				createGUI();
			}
		});
	}
	
	@SuppressWarnings("unchecked")
	private void createGUI() {
		Enumeration<CommPortIdentifier> portEnum = CommPortIdentifier.getPortIdentifiers();
		CBModelCommPortId model = new CBModelCommPortId(portEnum);
		
		JLabel lPort = new JLabel("Pilih Port :");
		cbCommPortId = new JComboBox<String>(model);
		cbCommPortId.setSelectedIndex(0);
		bConnect = new JToggleButton("Connect");
		bConnect.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent arg0) {
				int stateChange = arg0.getStateChange();
				switch(stateChange) {
				case ItemEvent.SELECTED :
					startConnection();
					break;
				case ItemEvent.DESELECTED :
					stopConnection();
					break;
				}
			}
		});
		
		taData = new JTextArea();
		DefaultCaret caret = (DefaultCaret)taData.getCaret();
		caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
		JScrollPane spData = new JScrollPane(taData);
		
		GroupLayout l = new GroupLayout(getContentPane());
		l.setAutoCreateGaps(true);
		l.setAutoCreateContainerGaps(true);
		l.setHorizontalGroup(l.createSequentialGroup()
			.addGroup(l.createParallelGroup()
				.addGroup(l.createSequentialGroup()
					.addComponent(lPort)
					.addComponent(cbCommPortId)
					.addComponent(bConnect)
				)
				.addComponent(spData)
			)
		);
		l.setVerticalGroup(l.createSequentialGroup()
			.addGroup(l.createParallelGroup(GroupLayout.Alignment.BASELINE)
				.addComponent(lPort)
				.addComponent(cbCommPortId)
				.addComponent(bConnect)
			)
			.addComponent(spData)
		);
		
		setTitle("SerialExc");
		setSize(200, 200);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setLocationRelativeTo(null);
		getContentPane().setLayout(l);
	}

	private void startConnection() {
		try {
			String portId = (String)cbCommPortId.getSelectedItem();
			CommPortIdentifier commPortId = CommPortIdentifier.getPortIdentifier(portId);
			CommPort commPort = commPortId.open(getClass().getName(), 2000);
			if(commPort instanceof SerialPort) {
				serialPort = (SerialPort)commPort;
				serialPort.setSerialPortParams(9600, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
				isOpened = true;
				in = serialPort.getInputStream();
				serialPort.addEventListener(this);
				serialPort.notifyOnDataAvailable(true);
			}
		}
		catch(Exception exc) {
			taData.append(exc.toString()+"\n");
			bConnect.setSelected(false);
		}
	}
	
	private void stopConnection() {
		if(isOpened) {
			serialPort.close();
			serialPort.removeEventListener();
		}
	}
	
	@Override
	public void serialEvent(SerialPortEvent arg0) {
		// TODO Auto-generated method stub
		int data;
		try {
			int len = 0;
			while((data=in.read()) > -1) {
				if(data==',') {
					break;
				}
				buffer[len++] = (byte)data;
			}
			taData.append(new String(buffer, 0, len) + "\n");
		}
		catch(IOException exc) {
			taData.append(exc.toString() + "\n");
		}
	}

}

class CBModelCommPortId implements ComboBoxModel<String> {

	private String selectedItem;
	private List<String> items;
	
	public CBModelCommPortId(Enumeration<CommPortIdentifier> arg0) {
		this.items = getCommPortIdNames(arg0);
	}
	
	@Override
	public void addListDataListener(ListDataListener l) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getElementAt(int index) {
		// TODO Auto-generated method stub
		return items.get(index);
	}

	@Override
	public int getSize() {
		// TODO Auto-generated method stub
		return items.size();
	}

	@Override
	public void removeListDataListener(ListDataListener l) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Object getSelectedItem() {
		// TODO Auto-generated method stub
		return (String)this.selectedItem;
	}

	@Override
	public void setSelectedItem(Object arg0) {
		// TODO Auto-generated method stub
		this.selectedItem = (String)arg0;
	}
	
	private List<String> getCommPortIdNames(Enumeration<CommPortIdentifier> arg0) {
		List<String> temp = new ArrayList<>();
		while(arg0.hasMoreElements()) {
			CommPortIdentifier commPortId = arg0.nextElement();
			if(commPortId.getPortType()==CommPortIdentifier.PORT_SERIAL) {
				temp.add(commPortId.getName());
			}
		}
		return temp;
	}
	
}