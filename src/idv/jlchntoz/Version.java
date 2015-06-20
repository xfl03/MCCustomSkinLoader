package idv.jlchntoz;

public class Version {
	private String version;
	private long longVersion;
	public Version(String version){
		this.version=version;
		longVersion=getLongVersion(version);
	}
	private long getLongVersion(String version){
		String[] temp=version.split("\\.");
		//System.out.println(version);
		long multi=1;
		long temp1=0;
		for(int i=temp.length-1;i>=0;i--){
			//System.out.println(temp[i]);
			temp1+=Integer.parseInt(temp[i])*multi;
			multi*=100;
		}
		//System.out.println(temp1);
		return temp1;
	}
	public String getVersion(){
		return version;
	}
	public long getLongVersion(){
		return longVersion;
	}
	public boolean isNewerThan(String compareVersion){
		return longVersion>getLongVersion(compareVersion);
	}
	public boolean isNewerThanOrEquals(String compareVersion){
		return longVersion>=getLongVersion(compareVersion);
	}
}
