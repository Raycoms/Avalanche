package com.constantine.communication.serveroperations;

import com.constantine.communication.ISender;

/**
 * Operation type to handle a communication to 1..n processes.
 */
public interface IOperation
{
    /**
     * Execute the OP on the ISender.
     * @param sender the sender object.
     */
    void executeOP(final ISender sender);
}


