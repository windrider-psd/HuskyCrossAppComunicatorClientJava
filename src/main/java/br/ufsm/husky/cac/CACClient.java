/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.ufsm.husky.cac;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

/**
 *
 * @author politecnico
 */
public class CACClient implements MqttCallback
{
    private void OnMessage(String obj)
    {
        //System.out.println(builtInMap.containsValue(Boolean.class));
        if (obj instanceof String)
        {
            try
            {
                ObjectMapper objectMapper = new ObjectMapper();
                Message message = objectMapper.readValue(obj, Message.class);

                if (message.getMessageType() == MessageType.COMMAND)
                {
                    Command nodeJSCommand = objectMapper.readValue(message.getArg(), Command.class);
                    InvokeRoutes(nodeJSCommand);
                }
                else
                {
                    System.out.println("hahahakkakakakka");
                    System.out.println(message.getArg());
                }
            }
            catch (IOException ex)
            {
                System.out.println("Invalid Command: " + ex.getMessage());
            }

        }

    }

    private MqttClient mqttClient;

    private Map<String, Map<OperationType, Set<Route>>> routeMapMap = new HashMap<>();
    private static final Map< String, Class> builtInMap = new HashMap< String, Class>();

    static
    {
        builtInMap.put("int", Integer.class);
        builtInMap.put("long", Long.class);
        builtInMap.put("double", Double.class);
        builtInMap.put("float", Float.class);
        builtInMap.put("bool", Boolean.class);
        builtInMap.put("char", Character.class);
        builtInMap.put("byte", Byte.class);
        builtInMap.put("void", Void.class);
        builtInMap.put("short", Short.class);
    }

    private boolean caseSentitiveCommands;
    private int writeCommandCount = 1;
    private int masterPort;
    private String masterIPAdress;

    public CACClient(String controllersPackage, String masterIPAdress, int masterPort)
    {
        this.masterPort = masterPort;
        this.masterIPAdress = masterIPAdress;

        SetUpRoutes(controllersPackage);
        this.Connect();

    }

    public void SendMessage(Message nodeJSMessage)
    {
        try
        {
            ObjectMapper objectMapper = new ObjectMapper();
            String jsonString = objectMapper.writeValueAsString(nodeJSMessage);
            MqttMessage mqttMessage = new MqttMessage(jsonString.getBytes());

            if (nodeJSMessage.getMessageType() == MessageType.COMMAND)
            {
                this.mqttClient.publish("master/command", mqttMessage);
            }
            else
            {
                this.mqttClient.publish("master/response", mqttMessage);
            }
        }
        catch (JsonProcessingException | MqttException ex)
        {
            Logger.getLogger(CACClient.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    private void SetUpRoutes(String packageName)
    {
        try
        {
            Set<Class> classes = new HashSet<>(Arrays.asList(getClasses(packageName)));
            //System.out.println(getClassesInPackage(packageName));
            classes.stream().filter(c ->
            {
                Annotation[] annotations = c.getAnnotations();
                for (Annotation annotation : annotations)
                {
                    if (annotation.annotationType().equals(Listener.class))
                    {
                        return true;
                    }
                }
                return false;
            }).collect(Collectors.toList());

            for (Class c : classes)
            {
                Object instance = c.newInstance();
                Set<Method> methods = new HashSet<>(Arrays.asList(c.getMethods()));

                methods = methods.stream().filter(method ->
                {
                    Annotation annotation = method.getAnnotation(ListenerRoute.class);
                    Parameter[] parameters = method.getParameters();

                    boolean valid = annotation != null;
                    // && (parameters.length == 0
                    //|| (parameters.length == 1 && !parameters[0].getType().isArray()));

                    return valid;

                }).collect(Collectors.toSet());

                for (Method method : methods)
                {

                    ListenerRoute listenerRoute = method.getAnnotation(ListenerRoute.class);

                    if (listenerRoute != null)
                    {

                        String commandPath = listenerRoute.CommandPath();

                        if (!routeMapMap.containsKey(commandPath))
                        {
                            Map<OperationType, Set<Route>> routeMap = new HashMap<>();
                            for (OperationType op : OperationType.values())
                            {
                                routeMap.put(op, new HashSet<>());
                            }

                            routeMapMap.put(commandPath, routeMap);
                        }
                        Route route = new Route(method, instance);
                        Set<Route> currentRoutes = routeMapMap.get(commandPath).get(listenerRoute.OperationType());
                        Route identicalRoute = null;
                        for (Route r : currentRoutes)
                        {
                            if (Route.AreIdenticalRoutes(route, r))
                            {
                                identicalRoute = r;
                                break;
                            }
                        }
                        if (identicalRoute != null)
                        {
                            System.out.println(route.getMethod() + "is identical to " + identicalRoute.getMethod());
                        }
                        else
                        {
                            routeMapMap.get(commandPath).get(listenerRoute.OperationType()).add(route);
                        }
                    }
                }
            }
        }
        catch (IOException | ClassNotFoundException | IllegalAccessException | InstantiationException | SecurityException ex)
        {
            ex.printStackTrace();
        }
           System.out.println("");
        

    }

    public void WriteCommand(String commandPath, OperationType operationType, Map<String, Object> args)
    {

        try
        {
            ObjectMapper objectMapper = new ObjectMapper();

            Map<String, String> argCommand = new HashMap<>();

            for (Map.Entry<String, Object> arg : args.entrySet())
            {
                String argJson = objectMapper.writeValueAsString(arg);
                argCommand.put(arg.getKey(), argJson);
            }

            Command nodeJSCommand = new Command(writeCommandCount++, commandPath, operationType, argCommand);

            String jsonCommand = objectMapper.writeValueAsString(nodeJSCommand);

            Message nodeJSMessage = new Message();
            nodeJSMessage.setArg(jsonCommand);
            nodeJSMessage.setMessageType(MessageType.COMMAND);

            this.SendMessage(nodeJSMessage);

           
        }
        catch (JsonProcessingException ex)
        {
            Logger.getLogger(CACClient.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    private Route FindFittingRoute(String commandPath, OperationType operationType, Map<String, String> args)
    {
        if (routeMapMap.containsKey(commandPath))
        {
            ObjectMapper objectMapper = new ObjectMapper();
            Set<Route> routes = routeMapMap.get(commandPath).get(operationType);
            Route stringRoute = null;
            for (Route route : routes)
            {
                Map<String, Class> argumentMap = route.getArgumentsMap();

                if (args.size() != argumentMap.size())
                {
                    continue;
                }
                if (args.isEmpty() && argumentMap.isEmpty())
                {
                    return route;
                }
                boolean valid = true;
                for (Map.Entry<String, String> entry : args.entrySet())
                {
                    if (argumentMap.containsKey(entry.getKey()))
                    {
                        if (isValidJSON(entry.getValue()))
                        {
                            try
                            {
                                objectMapper.readValue(entry.getValue(), argumentMap.get(entry.getKey()));
                            }
                            catch (IOException ex)
                            {
                                valid = false;
                                break;
                            }
                        }
                        else
                        {
                            Parameter parameter = route.getParameterWithKey(entry.getKey());
                            String arg = entry.getValue();

                            try
                            {

                                if (parameter == null)
                                {
                                    valid = false;
                                }

                                else if (parameter.getType().isPrimitive())
                                {
                                    if (parameter.getType().equals(boolean.class))
                                    {
                                        Boolean.parseBoolean(arg);
                                    }
                                    else if (parameter.getType().equals(byte.class))
                                    {
                                        Byte.parseByte(arg);
                                    }
                                    else if (parameter.getType().equals(short.class))
                                    {
                                        Short.parseShort(arg);
                                    }
                                    else if (parameter.getType().equals(int.class))
                                    {
                                       Integer.parseInt(arg);
                                    }
                                    else if (parameter.getType().equals(long.class))
                                    {
                                        Long.parseLong(arg);
                                    }
                                    else if (parameter.getType().equals(float.class))
                                    {
                                        Float.parseFloat(arg);
                                    }
                                    else if (parameter.getType().equals(double.class))
                                    {
                                        Double.parseDouble(arg);
                                    }
                                    valid = true;
                                }
                                else if (IsFromPrimitive(parameter.getType()))
                                {
                                    if (parameter.getType().equals(Boolean.class))
                                    {
                                        Boolean.valueOf(arg);
                                    }
                                    else if (parameter.getType().equals(Byte.class))
                                    {
                                        Byte.valueOf(arg);
                                    }
                                    else if (parameter.getType().equals(Short.class))
                                    {
                                        Short.valueOf(arg);
                                    }
                                    else if (parameter.getType().equals(Integer.class))
                                    {
                                       Integer.valueOf(arg);
                                    }
                                    else if (parameter.getType().equals(Long.class))
                                    {
                                        Long.valueOf(arg);
                                    }
                                    else if (parameter.getType().equals(Float.class))
                                    {
                                        Float.valueOf(arg);
                                    }
                                    else if (parameter.getType().equals(Double.class))
                                    {
                                        Double.valueOf(arg);
                                    }
                                }
                                else if (parameter.getType().equals(String.class))
                                {
                                    //stringRoute = route;
                                    valid = true;
                                }
                                else
                                {
                                    valid = false;
                                }
                            }
                            catch (Exception ex)
                            {
                                valid = false;
                                break;
                            }
                        }
                    }
                    else
                    {
                        valid = false;
                        break;
                    }
                }

                if (valid)
                {
                    return route;
                }
            }

            return stringRoute;

        }
        else
        {
            return null;
        }

    }

    public void InvokeRoutes(Command command)
    {

        Object respArg;
        ResponseStatus responseStatus;
        Route route = FindFittingRoute(command.getCommandPath(), command.getOperationType(), command.getArgs());
        ObjectMapper objectMapper = new ObjectMapper();
        if (route == null)
        {
            Message nodeJSMessage = new Message();
            CommandError commandError = new CommandError("NRT", "No route for " + command.getCommandPath());
            System.out.println("No route");
            nodeJSMessage.setMessageType(MessageType.RESPONSE);
            try
            {
                nodeJSMessage.setArg(objectMapper.writeValueAsString(commandError));
            }
            catch (JsonProcessingException ex)
            {
                Logger.getLogger(CACClient.class.getName()).log(Level.SEVERE, null, ex);
            }
            SendMessage(nodeJSMessage);
        }
        else
        {
            try
            {
                Object retorno = route.InvokeRoute(new ArrayList<>(command.getArgs().values()));
                Message nodeJSMessage = new Message();
                CommandResponse commandResponse = new CommandResponse(command.getCommandId(), ResponseStatus.OK, retorno);
                nodeJSMessage.setArg(objectMapper.writeValueAsString(commandResponse));
                nodeJSMessage.setMessageType(MessageType.RESPONSE);
                 SendMessage(nodeJSMessage);
            }
            catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | IOException ex)
            {
                Logger.getLogger(CACClient.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

    }

    public static boolean IsFromPrimitive(Class classz)
    {
        return builtInMap.containsValue(classz);
    }

    public static boolean isValidJSON(final String json)
    {
        try
        {
            String trim = json.trim();
            if (trim.isEmpty())
            {
                return false;
            }
            final ObjectMapper mapper = new ObjectMapper();
            JsonNode jnode = mapper.readTree(json);
            return jnode.fields().hasNext();
        }
        catch (IOException e)
        {
            return false;
        }
    }

    static Class[] getClasses(String packageName) throws ClassNotFoundException, IOException
    {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        assert classLoader != null;

        String path = packageName.replace('.', '/');
        Enumeration resources = classLoader.getResources(path);
        List dirs = new ArrayList();
        while (resources.hasMoreElements())
        {
            URL resource = (URL) resources.nextElement();
            String filePath = URLDecoder.decode(resource.getFile(), "UTF-8");
            dirs.add(new File(filePath));
        }
        ArrayList classes = new ArrayList();
        for (Object directory : dirs)
        {
            classes.addAll(findClasses((File) directory, packageName));
        }
        return (Class[]) classes.toArray(new Class[classes.size()]);
    }

    static List findClasses(File directory, String packageName) throws ClassNotFoundException
    {
        List classes = new ArrayList();

        if (!directory.exists())
        {
            return classes;
        }
        File[] files = directory.listFiles();

        for (File file : files)
        {

            if (file.isDirectory())
            {
                assert !file.getName().contains(".");
                classes.addAll(findClasses(file, packageName + "." + file.getName()));
            }
            else if (file.getName().endsWith(".class"))
            {
                classes.add(Class.forName(packageName + '.' + file.getName().substring(0, file.getName().length() - 6)));
            }
        }
        return classes;
    }

    private void Connect()
    {
        try
        {
            System.out.println("connecting");
            this.mqttClient = new MqttClient("tcp://" + masterIPAdress + ":" + masterPort, "java");
            MqttConnectOptions options = new MqttConnectOptions();
            options.setAutomaticReconnect(true);
            options.setCleanSession(true);
            options.setConnectionTimeout(1);
            mqttClient.connect(options);

            mqttClient.subscribe("java/response", (topic, msg) ->
            {
                byte[] payload = msg.getPayload();
                String json = new String(payload);
                this.OnMessage(json);
            });
            mqttClient.subscribe("java/command", (topic, msg) ->
            {
                byte[] payload = msg.getPayload();
                String json = new String(payload);
                this.OnMessage(json);
            });
        }
        catch (MqttException ex)
        {
            Logger.getLogger(CACClient.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void connectionLost(Throwable thrwbl)
    {
        this.Connect();

    }

    @Override
    public void messageArrived(String string, MqttMessage mm) throws Exception
    {
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken imdt)
    {
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
