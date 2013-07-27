/*
 * Copyright 2012 Harald Wellmann
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */
package org.ops4j.pax.exam.regression.cdi.ice;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.enterprise.util.AnnotationLiteral;
import javax.inject.Inject;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.sample3.ice.Chocolate;
import org.ops4j.pax.exam.sample3.ice.IceCreamService;
import org.ops4j.pax.exam.sample3.ice.Vanilla;

@RunWith(PaxExam.class)
public class VanillaTest {

    @Inject
    @Vanilla
    private IceCreamService vanilla;

    @Inject
    @Chocolate
    private IceCreamService chocolate;

    @Inject
    private IceCreamService defaultFlavour;

    @Inject
    @Any
    private Instance<IceCreamService> allFlavours;

    @Inject
    @Any
    private Instance<Object> instance;

    @Test
    public void checkVanillaFlavour() {
        assertThat(vanilla.getFlavour(), is("Vanilla"));
    }

    @Test
    public void checkChocolateFlavour() {
        assertThat(chocolate.getFlavour(), is("Chocolate"));
    }

    @Test
    public void checkDefaultFlavour() {
        assertThat(defaultFlavour.getFlavour(), is("Vanilla"));
    }

    @Test
    public void checkAllFlavours() {
        List<String> expectedFlavours = new ArrayList<String>(Arrays.asList("Vanilla", "Chocolate"));
        assertThat(allFlavours.isUnsatisfied(), is(false));
        assertThat(allFlavours.isAmbiguous(), is(true));
        int numFlavours = 0;
        Iterator<IceCreamService> it = allFlavours.iterator();
        while (it.hasNext()) {
            numFlavours++;
            String flavour = it.next().getFlavour();
            expectedFlavours.remove(flavour);
        }
        assertThat(numFlavours, is(2));
        assertThat(expectedFlavours.isEmpty(), is(true));
    }

    @SuppressWarnings("serial")
    @Test
    public void checkInstance() {
        AnnotationLiteral<Chocolate> qualifier = new AnnotationLiteral<Chocolate>() {
        };
        Instance<IceCreamService> chocolateInstance = allFlavours.select(qualifier);
        IceCreamService iceCreamService = chocolateInstance.get();
        assertThat(iceCreamService.getFlavour(), is("Chocolate"));

        instance.select(IceCreamService.class, qualifier).get();
    }
}
