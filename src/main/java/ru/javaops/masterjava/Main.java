package ru.javaops.masterjava;

import com.google.common.io.Resources;
import ru.javaops.masterjava.xml.schema.Group;
import ru.javaops.masterjava.xml.schema.ObjectFactory;
import ru.javaops.masterjava.xml.schema.Payload;
import ru.javaops.masterjava.xml.schema.User;
import ru.javaops.masterjava.xml.util.JaxbParser;
import ru.javaops.masterjava.xml.util.Schemas;
import ru.javaops.masterjava.xml.util.StaxStreamProcessor;
import ru.javaops.masterjava.xml.util.XsltProcessor;

import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.events.XMLEvent;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

/**
 * User: gkislin
 * Date: 05.08.2015
 *
 * @link http://caloriesmng.herokuapp.com/
 * @link https://github.com/JavaOPs/topjava
 */
public class Main {

    public static void main(String[] args) throws Exception {
        String project = args.length > 0 ? args[0] : null;
        if (project != null) {
            Set<User> usersJaxb = jaxbMethod(project);
            System.out.println(project + ":");
            usersJaxb.forEach(user -> System.out.println("\t" + user.getFullName()));

            Set<String> usersStax = staxMethod(project);
            System.out.println(project + ":");
            usersStax.forEach(s -> System.out.println("\t" + s));

            transformToHtml(project);
        }

    }

    private static Set<User> jaxbMethod(String project) {

        Payload payload = null;
        JaxbParser jaxbParser = new JaxbParser(ObjectFactory.class);
        jaxbParser.setSchema(Schemas.ofClasspath("payload.xsd"));

        try (InputStream stream = Resources.getResource("payload.xml").openStream()){
            payload = jaxbParser.unmarshal(stream);
        } catch (JAXBException | IOException e) {
            e.printStackTrace();
        }

        Set<User> users = new TreeSet<>(Comparator.comparing(User::getFullName));
        if (payload != null) {
            //get all groups from required project
            List<Group> groups = new ArrayList<>();
            payload.getProjects().getProject().stream().
                    filter(p -> p.getProjectName().equals(project)).
                    forEach(p -> groups.addAll(p.getGroups().getGroup()));

            //get users for required project
            payload.getUsers().getUser().
                    forEach(u -> u.getGroups()
                            .forEach(ug -> {
                                if (groups.contains(ug))
                                    users.add(u);
                            })
                    );
        }
        return users;
    }

    private static Set<String> staxMethod(String project) {

        Set<String> users = new TreeSet<>();
        List<String> groups = new ArrayList<>();

        try (StaxStreamProcessor processor =
                     new StaxStreamProcessor(Resources.getResource("payload.xml").openStream())){

            XMLStreamReader reader = processor.getReader();
            while (reader.hasNext()) {
                int xmlEvent = reader.next();
                if (xmlEvent == XMLEvent.START_ELEMENT) {

                    //put list of group to groups
                    if (staxEquals(reader, "Project")) {
                        processor.doUntil(XMLEvent.START_ELEMENT, "projectName");
                        if (reader.getElementText().equals(project)) {
                            do {
                                xmlEvent = reader.next();
                                if (xmlEvent == XMLEvent.START_ELEMENT && staxEquals(reader, "Group"))
                                    groups.add(reader.getAttributeValue(null, "id"));
                            }
                            while (!(xmlEvent == XMLEvent.END_ELEMENT && staxEquals(reader,"Project")));
                        }
                    }

                    //put pair <user.fullName, groups> in users
                    if (staxEquals(reader, "User")) {
                        // add groups from attr
                        String[] groupsArr = reader.getAttributeValue(null, "groups").split(" ");
                        // add name
                        processor.doUntil(XMLEvent.START_ELEMENT, "fullName");
                        String name = reader.getElementText();
                        for (String group: groupsArr) {
                            if (groups.contains(group))
                                users.add(name);
                        }
                    }
                }
            }
        } catch (XMLStreamException | IOException e) {
            e.printStackTrace();
        }
        return users;
    }

    public static void transformToHtml(String project) throws Exception {
        try (InputStream xslInputStream = Resources.getResource("projects.xsl").openStream();
             InputStream xmlInputStream = Resources.getResource("payload.xml").openStream()) {

            XsltProcessor processor = new XsltProcessor(xslInputStream);
            processor.setParam("ParamName", project);
            String html = processor.transform(xmlInputStream);
            Files.write(Paths.get("src\\main\\resources\\projects.html"), html.getBytes());
        }
    }

    private static boolean staxEquals(XMLStreamReader reader, String elementName) {
        return reader.getName().getLocalPart().equals(elementName);
    }
}