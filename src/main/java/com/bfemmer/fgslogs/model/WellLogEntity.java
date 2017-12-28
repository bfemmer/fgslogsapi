/*
The MIT License (MIT)

Copyright (c) 2017 Bill Femmer

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
 */

package com.bfemmer.fgslogs.model;

/**
 *
 * @author bfemmer
 */
public class WellLogEntity {
    private String id;
    private String wellLogId;
    private String wellNumber;
    
    public WellLogEntity(String id, String wellLogId, String wellNumber) {
        this.id = id;
        this.wellLogId = wellLogId;
        this.wellNumber = wellNumber;
    }
    
    /**
     * @return the id
     */
    public String getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * @return the wellLogId
     */
    public String getWellLogId() {
        return wellLogId;
    }

    /**
     * @param wellLogId the wellLogId to set
     */
    public void setWellLogId(String wellLogId) {
        this.wellLogId = wellLogId;
    }

    /**
     * @return the wellNumber
     */
    public String getWellNumber() {
        return wellNumber;
    }

    /**
     * @param wellNumber the wellNumber to set
     */
    public void setWellNumber(String wellNumber) {
        this.wellNumber = wellNumber;
    }
}
