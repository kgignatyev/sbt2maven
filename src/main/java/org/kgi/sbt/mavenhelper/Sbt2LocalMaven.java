package org.kgi.sbt.mavenhelper;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
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
        publisher.publish();
    }

    private void publish() {
        File target = new File("target");
        if( ! target.exists()){
            throw new RuntimeException("no directory " + target.getAbsolutePath() + " found, run sbt package first!");
        }
        Collection<File> pomFiles = FileUtils.listFiles(target, new IOFileFilter() {
                    @Override
                    public boolean accept(File file) {
                        return file.getName().endsWith("pom");
                    }

                    @Override
                    public boolean accept(File dir, String name) {
                        return true;
                    }
                }, new IOFileFilter() {

                    @Override
                    public boolean accept(File file) {
                        return file.isDirectory() && file.getName().startsWith("scala");//;
                    }

                    @Override
                    public boolean accept(File dir, String name) {
                        return true;
                    }
                }
        );

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
