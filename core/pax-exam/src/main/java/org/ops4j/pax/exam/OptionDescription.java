package org.ops4j.pax.exam;

/**
 * Central component that is usually implemented by a TestContainer.
 * It sheds some light also for validators to find what options are being dropped.
 */
public interface OptionDescription
{

    /**
     * @return All options that have been recognized and passed to the Test Container specific configuration
     */
    Option[] getUsedOptions();

    /**
     * @return All options that are not recognized and have been dropped.
     */
    Option[] getIgnoredOptions();


}
