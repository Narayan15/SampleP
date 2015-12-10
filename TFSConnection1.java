package reviewboard;

import java.net.URI;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.codec.binary.Base64;

import com.microsoft.tfs.core.TFSTeamProjectCollection;
import com.microsoft.tfs.core.clients.build.IBuildServer;
import com.microsoft.tfs.core.clients.versioncontrol.soapextensions.Change;
import com.microsoft.tfs.core.clients.versioncontrol.soapextensions.Changeset;
import com.microsoft.tfs.core.clients.versioncontrol.soapextensions.RecursionType;
import com.microsoft.tfs.core.clients.versioncontrol.specs.version.ChangesetVersionSpec;
import com.microsoft.tfs.core.clients.versioncontrol.specs.version.LatestVersionSpec;
import com.microsoft.tfs.core.clients.versioncontrol.specs.version.VersionSpec;
import com.microsoft.tfs.core.httpclient.Credentials;
import com.microsoft.tfs.core.httpclient.DefaultNTCredentials;
import com.microsoft.tfs.core.httpclient.UsernamePasswordCredentials;
import com.microsoft.tfs.core.util.CredentialsUtils;
import com.microsoft.tfs.core.util.URIUtils;

public class TFSConnection1 {
    
	 
	
    public static TFSTeamProjectCollection connectToTFS(String USERNAME, String PASSWORD, String COLLECTION_URL)
    {
    	
    	TFSTeamProjectCollection tpc = null;
        Credentials credentials;

        // In case no username is provided and the current platform supports
        // default credentials, use default credentials
        if ((USERNAME == null || USERNAME.length() == 0) && CredentialsUtils.supportsDefaultCredentials())
        {
            credentials = new DefaultNTCredentials();
        }
        else
        {
            credentials = new UsernamePasswordCredentials(USERNAME, PASSWORD);
        }

        URI httpProxyURI = null;

        /*if (HTTP_PROXY_URL != null && HTTP_PROXY_URL.length() > 0)
        {
            try
            {
                httpProxyURI = new URI(HTTP_PROXY_URL);
            }
            catch (URISyntaxException e)
            {
                // Do Nothing
            }
        }*/

        SnippetsSamplesConnectionAdvisor connectionAdvisor = new SnippetsSamplesConnectionAdvisor(httpProxyURI);

        tpc = new TFSTeamProjectCollection(URIUtils.newURI(COLLECTION_URL), credentials, connectionAdvisor);

        return tpc;
    }
    
    public static Map<String, RevisionInfo> getAllRevisions(TFSTeamProjectCollection tpc, int startRevision, int endRevision) {
        
        Map<String, RevisionInfo> revisionHistory = new HashMap<String, RevisionInfo>();
        Changeset[] chngset=null;
        try {
            VersionSpec startChangeSet = new ChangesetVersionSpec(startRevision);
            VersionSpec endChangeSet = new ChangesetVersionSpec(endRevision);
            chngset = tpc.getVersionControlClient().queryHistory("$/Anritsu", LatestVersionSpec.INSTANCE,
                    0, RecursionType.FULL, null, startChangeSet, null, Integer.MAX_VALUE, true, true, false, false); 

        } catch (Exception e) {

            // TODO Auto-generated catch block

            e.printStackTrace();
        }
        //System.out.println(chngset.length);
        for(Changeset ch : chngset)
        {
            //System.out.println("Change Set ID : "+ ch.getChangesetID());
            //System.out.println("Owner : "+ ch.getOwnerDisplayName());
            
            Change changes[]=ch.getChanges();
            
            Calendar cal = ch.getDate();
            int month = cal.get(Calendar.MONTH)+1;
			String monthTer=((month < 10) ? "0" : "")+month;
			int calDate = cal.get(Calendar.DATE);
			String dateTer = ((calDate<10)? "0": "")+calDate;
			int hour = cal.get(Calendar.HOUR_OF_DAY);
			String hourTer = ((hour<10)? "0": "")+hour;
			int minute = cal.get(Calendar.MINUTE);
			String minuteTer = ((minute<10)? "0": "")+minute;
			int seconds= cal.get(Calendar.SECOND);
			String secTer = ((seconds<10)? "0": "")+seconds;
			String dateTime = dateTer+"/"+monthTer+"/"+cal.get(Calendar.YEAR)+" "+hourTer+":"+minuteTer+":"+secTer;
			// System.out.println(cal.get(Calendar.DATE)+"/"+month+"/"+cal.get(Calendar.YEAR)+" "+cal.get(Calendar.HOUR_OF_DAY)+":"+cal.get(Calendar.MINUTE)+":"+cal.get(Calendar.SECOND));
         
            ArrayList<String> revisionFilePaths = new ArrayList<String>();
            for(Change chang:changes)
            {
                //System.out.println(chang.getItem().getServerItem());
                revisionFilePaths.add(chang.getItem().getServerItem());
                //System.out.println("Owner : "+         chang.getItem().getItemType().toString());
            }
            RevisionInfo revInfo = new RevisionInfo();
            revInfo.setDateTime(dateTime);
            revInfo.setAuthor(ch.getOwnerDisplayName());
            revInfo.setRevisionFiles(revisionFilePaths);
            revisionHistory.put("" + ch.getChangesetID(), revInfo);
         } 
        return revisionHistory;
    }
    public static boolean isLessThanV3BuildServer(IBuildServer buildServer)
    {
        if (buildServer.getBuildServerVersion().isLessThanV3())
        {
            System.out.println("This sample does not support TFS servers older than TFS2010");
            return true;
        }
        return false;
    }
    
    
    
    public static Map<String, RevisionInfo> getTFS(){
        
        String COLLECTION_URL = "http://tfs.techaspect.com:8080/tfs/Sitecore";
        String USERNAME = "backupadmin";
        String PASSWORD = new String(new Base64().decode("L0NyZUB0aXZlJTAhLw==".getBytes())); 
        TFSTeamProjectCollection tpc = connectToTFS(USERNAME, PASSWORD, COLLECTION_URL);
        String reviString= null;
        Map<String, RevisionInfo> hmap = getAllRevisions(tpc,6940,100);
        
        /*for (java.util.Map.Entry<String, ArrayList<String>> entry : getAllRevisions(tpc,6940,100).entrySet()) {
            ArrayList<String> revisionFiles = entry.getValue();
            for (int i=0;i<revisionFiles.size();i++)
            {
            	 reviString = revisionFiles.get(i);
                //System.out.println(revisionFiles.get(i));
            }
            RevisionInfo revInfo = new RevisionInfo();
            revInfo.setAuthor(ch.getOwnerDisplayName());
			revInfo.setRevisionFiles(revisionFiles);
            	hmap.put(entry.getKey(), revInfo);
       }*/
        return hmap;
        /*for (Project project : tpc.getWorkItemClient().getProjects())
        {
            System.out.println(project.getName());
        }
        
        Changeset[] chngset=null;
        try {

            chngset = tpc.getVersionControlClient().queryHistory("$/Anritsu", LatestVersionSpec.INSTANCE,
                    0, RecursionType.FULL, null, null, null, Integer.MAX_VALUE, true, true, false, false); 

        } catch (Exception e) {

            // TODO Auto-generated catch block

            e.printStackTrace();
        }
        System.out.println(chngset.length);
        int index = 0;
        for(Changeset ch : chngset)
        {
            System.out.println("Change Set ID : "+ ch.getChangesetID());

            System.out.println("Owner : "+ ch.getOwnerDisplayName());

            Change changes[]=ch.getChanges();

            System.out.println("Date : "+ new     Date(ch.getDate().getTimeInMillis()));

            for(Change chang:changes)
            {
                System.out.println(chang.getItem().getServerItem());;
                //System.out.println("Owner : "+         chang.getItem().getItemType().toString());
            }
            if (index++ == 10)
            {
                break;
            }
         }  */

    }
    
    public static void main(String[] args){
    	getTFS();
    }

}

