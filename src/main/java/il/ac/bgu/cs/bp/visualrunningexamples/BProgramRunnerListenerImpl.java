package il.ac.bgu.cs.bp.visualrunningexamples;

import il.ac.bgu.cs.bp.bpjs.execution.listeners.BProgramRunnerListener;
import il.ac.bgu.cs.bp.bpjs.model.BEvent;
import il.ac.bgu.cs.bp.bpjs.model.BProgram;
import il.ac.bgu.cs.bp.bpjs.model.BThreadSyncSnapshot;
import il.ac.bgu.cs.bp.bpjs.model.FailedAssertion;
import javax.swing.SwingUtilities;

/**
 * This listener is used to update the UI when the BProgram is running.
 * 
 * @author michael
 */
class BProgramRunnerListenerImpl implements BProgramRunnerListener {
    
    private final MainWindowCtrl mwCtrl;

    public BProgramRunnerListenerImpl(final MainWindowCtrl mwCtrl) {
        this.mwCtrl = mwCtrl;
    }

    @Override
    public void starting(BProgram bprog) {
        mwCtrl.addToLog("Starting...");
    }

    @Override
    public void started(BProgram bp) {
        mwCtrl.addToLog("Started");
    }

    @Override
    public void superstepDone(BProgram bp) {
        mwCtrl.addToLog("Superstep done - awaiting external events");
        mwCtrl.setInProgress(false); // generally not true, but here it is, since there's no environment.
    }

    @Override
    public void ended(BProgram bp) {
        mwCtrl.addToLog("Ended");
        mwCtrl.setInProgress(false);
    }

    @Override
    public void assertionFailed(BProgram bp, FailedAssertion theFailedAssertion) {
        mwCtrl.addToLog("Failed Assertion: " + theFailedAssertion.getMessage());
    }

    @Override
    public void bthreadAdded(BProgram bp, BThreadSyncSnapshot theBThread) {
        mwCtrl.addToLog(" + " + theBThread.getName() + " added");
    }

    @Override
    public void bthreadRemoved(BProgram bp, BThreadSyncSnapshot theBThread) {
        mwCtrl.addToLog(" - " + theBThread.getName() + " removed");
    }

    @Override
    public void bthreadDone(BProgram bp, BThreadSyncSnapshot theBThread) {
        mwCtrl.addToLog(" - " + theBThread.getName() + " completed");
    }

    @Override
    public void eventSelected(BProgram bp, BEvent theEvent) {
        SwingUtilities.invokeLater(() -> {
            String eventName = theEvent.getName();
            if (eventName.startsWith("Enter")) {
                String[] comps = eventName.substring(7).split(",");
                comps[1] = comps[1].replace(")", "");
                mwCtrl.mazeTableModel.addCellEntry(Integer.valueOf(comps[1]), Integer.valueOf(comps[0]));
            }
            mwCtrl.addToLog("Event: " + theEvent.toString());
        });
    }
    
}
