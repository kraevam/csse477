package ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.util.HashMap;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import plugin.ILoadableApplication;
import plugin.PluginManager;
import extension.IPluginListenerExtension;

public class BasicBrahmaUI implements IPluginListenerExtension {
	///////////////////////////////////////////////////////////////////////////
	private JFrame frame;
	private List<ILoadableApplication> plugins;
	private JPanel contentPane;
	private JLabel bottomLabel;
	private JList<DefaultListModel<String>> sideList;
	private DefaultListModel<String> listModel;
	private JPanel centerEnvelope;
	
	// For holding registered plugin
	private HashMap<String, ILoadableApplication> idToPlugin;
	private ILoadableApplication currentPlugin;
	
	// Plugin manager
	PluginManager pluginManager;
	
	public BasicBrahmaUI() {
		idToPlugin = new HashMap<String, ILoadableApplication>();
		
		// Lets create the elements that we will need
		frame = new JFrame("Pluggable Board Application");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		contentPane = (JPanel)frame.getContentPane();
		contentPane.setPreferredSize(new Dimension(700, 500));
		bottomLabel = new JLabel("No plugins registered yet!");
		
		listModel = new DefaultListModel<String>();
		sideList = new JList(listModel);
		sideList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		sideList.setLayoutOrientation(JList.VERTICAL);
		JScrollPane scrollPane = new JScrollPane(sideList);
		scrollPane.setPreferredSize(new Dimension(100, 50));
		
		// Create center display area
		centerEnvelope = new JPanel(new BorderLayout());
		centerEnvelope.setBorder(BorderFactory.createLineBorder(Color.black, 5));
		
		// Lets lay them out, contentPane by default has BorderLayout as its layout manager
		contentPane.add(centerEnvelope, BorderLayout.CENTER);
		contentPane.add(scrollPane, BorderLayout.EAST);
		contentPane.add(bottomLabel, BorderLayout.SOUTH);
		
		// Add action listeners
		sideList.getSelectionModel().addListSelectionListener(this.getLSListener());
		
		// Start the plugin manager now that the core is ready
		try {
			this.pluginManager = new PluginManager();
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		Thread thread = new Thread(this.pluginManager);
		thread.start();
	}
	
	private ListSelectionListener getLSListener() {
		return new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				// If the list is still updating, return
				if(e.getValueIsAdjusting())
					return;
				
				// List has finalized selection, let's process further
				int index = sideList.getSelectedIndex();
				String id = listModel.elementAt(index);
				ILoadableApplication plugin = idToPlugin.get(id);
				
				if(plugin == null || plugin.equals(currentPlugin))
					return;
				
				// Stop previously running plugin
				if(currentPlugin != null)
					currentPlugin.stop();
				
				// The newly selected plugin is our current plugin
				currentPlugin = plugin;
				
				// Clear previous working area
				centerEnvelope.removeAll();
				
				// Create new working area
				JPanel centerPanel = new JPanel();
				centerEnvelope.add(centerPanel, BorderLayout.CENTER); 
				
				// Ask plugin to layout the working area
				// TODO: This whole class should be remodeled to be a UI Extension...
				currentPlugin.layout(centerPanel);
				contentPane.revalidate();
				contentPane.repaint();
				
				// Start the plugin
				currentPlugin.start();
				
				bottomLabel.setText("The " + currentPlugin.getId() + " is running!");
			}
		};
	}
	
	public void start() {
		EventQueue.invokeLater(new Runnable() {
			public void run()
			{
				frame.pack();
				frame.setVisible(true);
			}
		});
	}
	
	public void stop() {
		EventQueue.invokeLater(new Runnable() {
			public void run()
			{
				frame.setVisible(false);
			}
		});
	}
	
	public void addPlugin(ILoadableApplication plugin) {
		this.idToPlugin.put(plugin.getId(), plugin);
		this.listModel.addElement(plugin.getId());
		this.bottomLabel.setText("The " + plugin.getId() + " plugin has been recently added!");
	}
	
	public void removePlugin(String id) {
		ILoadableApplication plugin = this.idToPlugin.remove(id);
		this.listModel.removeElement(id);
		
		// Stop the plugin if it is still running
		plugin.stop();

		this.bottomLabel.setText("The " + plugin.getId() + " plugin has been recently removed!");
	}
	
	///////////////////////////////////////////////////////
	
	@Override
	public void pluginLoaded(ILoadableApplication plugin) {
		addPlugin(plugin);
	}

	@Override
	public void pluginUnloaded(ILoadableApplication plugin) {
		removePlugin(plugin.getId());
	}
	
	

}
