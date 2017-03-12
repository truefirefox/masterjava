package ru.javaops.masterjava;

import org.xml.sax.SAXException;
import ru.javaops.masterjava.xml.schema.Group;
import ru.javaops.masterjava.xml.schema.ObjectFactory;
import ru.javaops.masterjava.xml.schema.Payload;
import ru.javaops.masterjava.xml.schema.User;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import javax.xml.validation.SchemaFactory;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.*;

/**
 * User: gkislin
 * Date: 05.08.2015
 *
 * @link http://caloriesmng.herokuapp.com/
 * @link https://github.com/JavaOPs/topjava
 */
public class Main {
    private static File fileXML = new File("src\\main\\resources\\payload.xml");
    private static File fileXSD = new File("src\\main\\resources\\payload.xsd");

    public static void main(String[] args) {
        String project = args[0];
        if (project != null) {
            jaxbMethod(project);
            staxMethod(project);
        }
    }

    private static void jaxbMethod(String project) {
        Set<User> users = new TreeSet<>(Comparator.comparing(User::getFullName));
        List<Group> groups = new ArrayList<>();

        Payload payload = null;
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(ObjectFactory.class);
            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
            jaxbUnmarshaller.setSchema(SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI).newSchema(fileXSD));
            payload = (Payload) jaxbUnmarshaller.unmarshal(fileXML);
        } catch (JAXBException | SAXException e) {
            e.printStackTrace();
        }

        if (payload != null) {
            //get all groups from required project
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
        System.out.println(project + ":");
        users.forEach(user -> System.out.println("\t" + user.getFullName()));
    }

    // this method doesn't work properly with guava through *.jar

   /* private static void jaxbMethod(String project) {
        Set<User> users = new TreeSet<>(Comparator.comparing(User::getFullName));
        List<Group> groups = new ArrayList<>();
        Payload payload = null;

        JaxbParser jaxbParser = new JaxbParser(ObjectFactory.class);
        jaxbParser.setSchema(Schemas.ofClasspath("payload.xsd"));

        try {
             payload = jaxbParser.unmarshal(
                    Resources.getResource("payload.xml").openStream());

        } catch (JAXBException | IOException e) {
            e.printStackTrace();
        }

        if (payload != null) {
            //get all groups from required project
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
        System.out.println(project + ":");
        users.forEach(user -> System.out.println("\t" + user.getFullName()));
    }*/


    private static void staxMethod(String project) {

        Map<String, List<String>> users = new HashMap<>();
        List<String> groups = new ArrayList<>();

        XMLInputFactory xmlInputFactory = XMLInputFactory.newInstance();
        try {
            XMLEventReader xmlEventReader = xmlInputFactory.createXMLEventReader(new FileInputStream(fileXML));
            while(xmlEventReader.hasNext()) {
                XMLEvent xmlEvent = xmlEventReader.nextEvent();
                if (xmlEvent.isStartElement()) {
                    StartElement startElement = xmlEvent.asStartElement();

                    //put pair <user.fullName, groups> in users
                    if (startElement.getName().getLocalPart().equals("User")) {
                        String[] groupsArr = startElement.getAttributeByName(QName.valueOf("groups")).getValue().split(" ");
                        do {
                            xmlEvent = xmlEventReader.nextEvent();
                        } while (!xmlEvent.isStartElement());
                        startElement = xmlEvent.asStartElement();
                        if (startElement.getName().getLocalPart().equals("fullName")) {
                            String fullName = xmlEventReader.getElementText();
                            users.put(fullName, Arrays.asList(groupsArr));
                        }
                    }

                    //put list of group to groups
                    if (startElement.getName().getLocalPart().equals("Project") ) {
                        do {
                            xmlEvent = xmlEventReader.nextEvent();
                        } while (!xmlEvent.isStartElement());
                        startElement = xmlEvent.asStartElement();
                        if (startElement.getName().getLocalPart().equals("projectName")
                                && xmlEventReader.getElementText().equals(project)) {
                            while (true) {
                                xmlEvent = xmlEventReader.nextEvent();
                                if (xmlEvent.isEndElement() && xmlEvent.asEndElement().getName().getLocalPart().equals("Project"))
                                    break;
                                if (xmlEvent.isStartElement() && xmlEvent.asStartElement().getName().getLocalPart().equals("Group"))
                                    groups.add(xmlEvent.asStartElement().getAttributeByName(QName.valueOf("id")).getValue());
                            }
                        }
                    }

                }
            }
        } catch (FileNotFoundException | XMLStreamException e) {
            e.printStackTrace();
        }

        //find participants for project
        Set<String> usersToPrint = new TreeSet<>();
        groups.forEach(g ->
            users.forEach((String u, List<String> ug) -> {
                if (users.get(u).contains(g)) {
                    usersToPrint.add(u);
                }
            }));

        System.out.println(project + ":");
        usersToPrint.forEach(user -> System.out.println("\t" + user));
    }
}