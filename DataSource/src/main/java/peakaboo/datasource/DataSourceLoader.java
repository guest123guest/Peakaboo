package peakaboo.datasource;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import bolt.plugin.BoltPluginLoader;
import bolt.plugin.ClassInheritanceException;
import bolt.plugin.ClassInstantiationException;
import commonenvironment.Env;
import peakaboo.common.Version;
import peakaboo.datasource.plugins.PlainText;

public class DataSourceLoader
{

	private static BoltPluginLoader<PluginDataSource> loader;
	
	public synchronized static BoltPluginLoader<PluginDataSource> getPluginLoader() {
		if (loader == null) {
			try {
				initLoader();
			} catch (ClassInheritanceException | ClassInstantiationException e) {
				e.printStackTrace();
			}
		}
		return loader;
	}
	
	private synchronized static void initLoader() throws ClassInheritanceException, ClassInstantiationException {
		
		BoltPluginLoader<PluginDataSource> newLoader = new BoltPluginLoader<PluginDataSource>(PluginDataSource.class);  
		
		//load local jars
		newLoader.register();
		
		//load jars in the app data directory
		File appDataDir = Env.appDataDirectory(Version.program_name, "Plugins");
		appDataDir.mkdirs();
		newLoader.register(appDataDir);
			
		
		//register built-in plugins
		newLoader.registerPlugin(PlainText.class);
					
		loader = newLoader;
	}
	
	public synchronized static List<DataSource> getDataSourcePlugins()
	{

		try
		{
			
			if (loader == null)
			{
				initLoader();
			}
			
			List<PluginDataSource> filters = loader.getNewInstancesForAllPlugins();
			
			Collections.sort(filters, new Comparator<PluginDataSource>() {

				@Override
				public int compare(PluginDataSource f1, PluginDataSource f2)
				{
					return f1.getFileFormat().getFormatName().compareTo(f1.getFileFormat().getFormatName());
				}});
			
			return new ArrayList<>(filters);
			
		} catch (ClassInheritanceException | ClassInstantiationException e) {
			e.printStackTrace();
		}
		
		//failure -- return empty list
		List<DataSource> plugins = new ArrayList<DataSource>();
		return plugins;
	}

	
	
	public static void main(String[] args)
	{
		getDataSourcePlugins();
	}
	
}
