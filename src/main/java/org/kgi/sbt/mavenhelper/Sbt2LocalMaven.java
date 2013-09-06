package org.kgi.sbt.mavenhelper;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.io.FileUtils;
import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;
import java.io.File;
import java.util.Collection;

/**
 * User: kgignatyev
 * Date: 7/15/13
 */
public class Sbt2LocalMaven {

    public static void main(String[] args) {
        Sbt2LocalMaven publisher = new Sbt2LocalMaven();
        String dir = ".";
        if( args.length >0){
            dir = args[0];
        }
        publisher.publish(dir);
    }

    private void publish( String dirToInvestigate ) {
        File startDir = new File(dirToInvestigate);
        final int startDirNameLength = startDir.getAbsolutePath().length();
        Collection<File> pomFileCandidates = FileUtils.listFiles(startDir, new String[]{"pom"},true  ) ;
        Collection<File> pomFiles = CollectionUtils.select(pomFileCandidates, new Predicate(){
            @Override
            public boolean evaluate(Object o) {
                File f = (File) o;
                String relativeName = f.getAbsolutePath().substring(startDirNameLength);
                System.out.println("Investigate::" + relativeName);
                return relativeName.indexOf("target")!=-1;
            }
        });

        if( pomFiles.size() == 0){
            throw new RuntimeException("No POM files found, did you rum 'sbt publish-local' ?");
        }
        for (File pomFile : pomFiles) {
            copyFilesForPom( pomFile );
        }
    }

    private void copyFilesForPom(File pomFile) {
        try {
            Document pom = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(pomFile);
            XPath xPath =  XPathFactory.newInstance().newXPath();
            String artifactId = xPath.evaluate("/project/artifactId/text()", pom);
            String group = xPath.evaluate("/project/groupId/text()", pom);
            String version = xPath.evaluate("/project/version/text()", pom);
            //System.out.println("artifactId = " + artifactId + "/" + group + "/" + version);

            String baseName = pomFile.getParentFile().getAbsolutePath() + "/" + artifactId + "-" + version;
           // System.out.println("baseName = " + baseName);

            StringBuilder mvnCommand = new StringBuilder("mvn install:install-file -Dfile=");
            mvnCommand.append( baseName ).append(".jar ")
                    .append("-DpomFile=").append(baseName).append(".pom ");
            if( new File(baseName+"-sources.jar").exists()){
                mvnCommand.append("-Dsources=").append(baseName).append("-sources.jar ");
            }
            if( new File(baseName+"-javadoc.jar").exists()){
                            mvnCommand.append("-Djavadoc=").append(baseName).append("-javadoc.jar ");
                        }
            mvnCommand.append(" -DgroupId=").append(group)
                    .append(" -DartifactId=").append(artifactId)
                    .append(" -Dversion=" ).append(version);
            System.out.println("mvnCommand = " + mvnCommand);
            CommandLine cmdLine = CommandLine.parse(mvnCommand.toString());
            DefaultExecutor executor = new DefaultExecutor();
            int exitValue = executor.execute(cmdLine);
            System.out.println("exitValue = " + exitValue);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
