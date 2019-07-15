/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.ufsm.husky.cac;

import static br.ufsm.husky.cac.CACClient.IsFromPrimitive;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author politecnico
 */
class Route
{

    private Method method;
    private Object instance;
    private final Map<String, Class> argumentsMap;
    
    public Route(Method method, Object instance)
    {
        this.method = method;
        this.instance = instance;
        this.method.setAccessible(true);
        this.argumentsMap = CreateArgumentsMap();
    }
    
    

    public Object InvokeRoute(List<String> argsMap) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, IOException
    {
        int count = method.getParameterCount();
        if (count == 0 && argsMap.isEmpty())
        {
            return this.method.invoke(instance);
        } 
        else
        {
            ObjectMapper objectMapper = new ObjectMapper();
            Object[] args = new Object[argsMap.size()];
            int i = 0;
            
            for(Map.Entry<String, Class> entry : this.getArgumentsMap().entrySet())
            {
                
                if(CACClient.isValidJSON(argsMap.get(i)))
                {
                    args[i] = objectMapper.readValue(argsMap.get(i), entry.getValue());
                }
                else
                {
                    Parameter parameter = this.getParameterWithKey(entry.getKey());
                    Object value = null;
                    String arg = argsMap.get(i);
                    if (parameter.getType().isPrimitive())
                    {
                        if (parameter.getType().equals(boolean.class))
                        {
                            value = Boolean.parseBoolean(arg);
                        }
                        else if (parameter.getType().equals(byte.class))
                        {
                            value = Byte.parseByte(arg);
                        }
                        else if (parameter.getType().equals(short.class))
                        {
                            value = Short.parseShort(arg);
                        }
                        else if (parameter.getType().equals(int.class))
                        {
                            value = Integer.parseInt(arg);
                        }
                        else if (parameter.getType().equals(long.class))
                        {
                            value = Long.parseLong(arg);
                        }
                        else if (parameter.getType().equals(float.class))
                        {
                            value = Float.parseFloat(arg);
                        }
                        else if (parameter.getType().equals(double.class))
                        {
                            value = Double.parseDouble(arg);
                        }
                    }
                    else if (IsFromPrimitive(parameter.getType()))
                    {
                        if (parameter.getType().equals(Boolean.class))
                        {
                            value = Boolean.valueOf(arg);
                        }
                        else if (parameter.getType().equals(Byte.class))
                        {
                            value = Byte.valueOf(arg);
                        }
                        else if (parameter.getType().equals(Short.class))
                        {
                            value = Short.valueOf(arg);
                        }
                        else if (parameter.getType().equals(Integer.class))
                        {
                            value = Integer.valueOf(arg);
                        }
                        else if (parameter.getType().equals(Long.class))
                        {
                            value = Long.valueOf(arg);
                        }
                        else if (parameter.getType().equals(Float.class))
                        {
                            value = Float.valueOf(arg);
                        }
                        else if (parameter.getType().equals(Double.class))
                        {
                            value = Double.valueOf(arg);
                        }
                    }
                    else if (parameter.getType().equals(String.class))
                    {
                        value = arg;
                    }
                    args[i] = value;
                }
                
                i++;
            }
            
            return this.method.invoke(instance, args);
        }
    }

   
    
    public Method getMethod()
    {
        return method;
    }

    public void setMethod(Method method)
    {
        this.method = method;
    }

    public Object getInstance()
    {
        return instance;
    }

    public void setInstance(Object instance)
    {
        this.instance = instance;
    }

    public Map<String, Class> getArgumentsMap()
    {
        return argumentsMap;
    }

    
    public static boolean AreIdenticalRoutes(Route a, Route b)
    {
        Map<String, Class> a_args = a.getArgumentsMap();
        Map<String, Class> b_args = b.getArgumentsMap();
        
        if(a_args.size() != b_args.size() && !a_args.isEmpty())
        {
            return false;
        }
        else
        {
            for (Map.Entry<String, Class> entry : a_args.entrySet()) {
                String key = entry.getKey();
                Class value = entry.getValue();
                
                if(b_args.containsKey(key) && b_args.get(key).equals(value))
                {
                    return true;
                }
            }
            return false;
        }
    }
    private Map<String, Class> CreateArgumentsMap()
    {
        Map<String, Class> map = new LinkedHashMap<>();
        
        Parameter[] parameters = method.getParameters();
        
        for(Parameter parameter : parameters)
        {
            map.put(parameter.getName(), parameter.getType());
        }
        
        return map;
    }
    
    public Parameter getParameterWithKey(String key)
    {
        Parameter[] parameters = method.getParameters();
        for(Parameter parameter : parameters)
        {
            if(parameter.getName().equals(key))
            {
                return parameter;
            }
        }
        return null;
    }
}
