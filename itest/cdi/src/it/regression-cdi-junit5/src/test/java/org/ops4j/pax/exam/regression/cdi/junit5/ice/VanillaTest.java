/*
 * Copyright 2017 Harald Wellmann
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
package org.ops4j.pax.exam.regression.cdi.junit5.ice;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.enterprise.util.AnnotationLiteral;
import javax.inject.Inject;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.ops4j.pax.exam.invoker.junit5.PaxExam;
import org.ops4j.pax.exam.sample3.ice.Chocolate;
import org.ops4j.pax.exam.sample3.ice.IceCreamService;
import org.ops4j.pax.exam.sample3.ice.Vanilla;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerClass;

@ExtendWith(PaxExam.class)
@ExamReactorStrategy(PerClass.class)
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
    
    @BeforeEach
    public void beforeEach() {
    	System.out.println("*** before each");
    }
    
    @BeforeAll
    public static void beforeAll() {
    	System.out.println("*** before all");
    }
    
    @AfterEach
    public void afterEach() {
    	System.out.println("*** after each");
    }
    
    @AfterAll
    public static void afterAll() {
    	System.out.println("*** after all");
    }
    

    @Test
    public void checkVanillaFlavour() {
        assertThat(vanilla.getFlavour()).isEqualTo("Vanilla");
    }

    @Test
    public void checkChocolateFlavour() {
        assertThat(chocolate.getFlavour()).isEqualTo("Chocolate");
    }

    @Test
    public void checkDefaultFlavour() {
        assertThat(defaultFlavour.getFlavour()).isEqualTo("Vanilla");
    }

    @Test
    public void checkAllFlavours() {
        List<String> expectedFlavours = new ArrayList<String>(Arrays.asList("Vanilla", "Chocolate"));
        assertThat(allFlavours.isUnsatisfied()).isFalse();
        assertThat(allFlavours.isAmbiguous()).isTrue();
        int numFlavours = 0;
        Iterator<IceCreamService> it = allFlavours.iterator();
        while (it.hasNext()) {
            numFlavours++;
            String flavour = it.next().getFlavour();
            expectedFlavours.remove(flavour);
        }
        assertThat(numFlavours).isEqualTo(2);
        assertThat(expectedFlavours).isEmpty();
    }

    @SuppressWarnings("serial")
    @Test
    public void checkInstance() {
        AnnotationLiteral<Chocolate> qualifier = new AnnotationLiteral<Chocolate>() {
        };
        Instance<IceCreamService> chocolateInstance = allFlavours.select(qualifier);
        IceCreamService iceCreamService = chocolateInstance.get();
        assertThat(iceCreamService.getFlavour()).isEqualTo("Chocolate");

        instance.select(IceCreamService.class, qualifier).get();
    }
}
