package com.constantine.comm.communication.serveroperations;

import com.constantine.comm.communication.ISender;

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


