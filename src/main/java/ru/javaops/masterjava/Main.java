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
import javax.xml.validation.SchemaFactory;
import java.io.File;
import java.util.*;

/**
 * User: gkislin
 * Date: 05.08.2015
 *
 * @link http://caloriesmng.herokuapp.com/
 * @link https://github.com/JavaOPs/topjava
 */
public class Main {
    public static void main(String[] args) {
        String project = args[0];

        jaxbMethod(project);
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

    private static void jaxbMethod(String project) {
        Set<User> users = new TreeSet<>(Comparator.comparing(User::getFullName));
        List<Group> groups = new ArrayList<>();

        File fileXML = new File("src\\main\\resources\\payload.xml");
        File fileXSD = new File("src\\main\\resources\\payload.xsd");

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
}
