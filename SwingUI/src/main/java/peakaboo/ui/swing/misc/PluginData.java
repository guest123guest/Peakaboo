package peakaboo.ui.swing.misc;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

import bolt.plugin.BoltPlugin;
import bolt.plugin.BoltPluginController;
import swidget.icons.IconFactory;
import swidget.icons.IconSize;
import swidget.widgets.ClearPanel;
import swidget.widgets.TextWrapping;

public class PluginData extends ClearPanel {
	
	BoltPluginController<? extends BoltPlugin> plugin;
	
	JLabel name;
	JLabel version;
	JLabel description;
	JLabel enabled;
	JLabel type;
	
	public PluginData(BoltPluginController<? extends BoltPlugin> plugin) {
		super(new BorderLayout());
		this.plugin = plugin;
		
		this.setMinimumSize(new Dimension(500, 100));
		
		setOpaque(true);
		setBackground(Color.WHITE);
		setBorder(new LineBorder(Color.GRAY));
		
		
		name = new JLabel(plugin.getName());
		version = new JLabel(plugin.getVersion());
		description = new JLabel(TextWrapping.wrapTextForMultiline(plugin.getDescription(), 375));
		enabled = new JLabel(new ImageIcon(IconFactory.getImageIcon(plugin.isEnabled() ? "choose-ok" : "choose-cancel", IconSize.BUTTON).getImage()));
		type = new JLabel(plugin.getPluginClass().getSimpleName());
		
		
		name.setBorder(new EmptyBorder(10, 10, 20, 10));
		name.setBorder(new EmptyBorder(5, 5, 5, 5));
		version.setBorder(new EmptyBorder(5, 5, 5, 5));
		description.setBorder(new EmptyBorder(5, 5, 5, 5));
		enabled.setBorder(new EmptyBorder(5, 5, 5, 5));
		type.setBorder(new EmptyBorder(5, 5, 5, 5));
		
		
		name.setFont(name.getFont().deriveFont(20f));
		description.setForeground(Color.GRAY);
		version.setForeground(Color.GRAY);
		type.setForeground(Color.GRAY);
		
		
		this.add(name, BorderLayout.CENTER);
		this.add(version, BorderLayout.EAST);
		this.add(description, BorderLayout.SOUTH);
		this.add(enabled, BorderLayout.WEST);
		this.add(type, BorderLayout.NORTH);
		
		
	}

}
