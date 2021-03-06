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

import com.bfemmer.fgslogs.viewmodel.FormationViewModel;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author bfemmer
 */
public class FormationEntity extends WellLogEntity {
    private List<FormationViewModel> formations;

    public FormationEntity(String id, String wellLogId, String wellNumber) {
        super(id, wellLogId, wellNumber);
        
        formations = new ArrayList<>();
    }

    /**
     * @return the formations
     */
    public List<FormationViewModel> getFormations() {
        return formations;
    }

    /**
     * @param formations the formations to set
     */
    public void setFormations(List<FormationViewModel> formations) {
        this.formations = formations;
    }   
}
