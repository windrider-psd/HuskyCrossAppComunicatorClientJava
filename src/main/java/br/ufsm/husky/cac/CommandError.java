/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.ufsm.husky.cac;

/**
 *
 * @author politecnico
 */
public class CommandError
{
    private String code;
    private String message;

    public CommandError(String code, String message)
    {
        this.code = code;
        this.message = message;
    }
    
    public static CommandError FromException(Exception ex)
    {
        return new CommandError(ex.getClass().getSimpleName(), ex.getMessage());
    }
    
    public static CommandError FromException(Throwable ex)
    {
        return new CommandError(ex.getClass().getSimpleName(), ex.getMessage());
    }

    public String getCode()
    {
        return code;
    }

    public void setCode(String code)
    {
        this.code = code;
    }

    public String getMessage()
    {
        return message;
    }

    public void setMessage(String message)
    {
        this.message = message;
    }
    
    
}
