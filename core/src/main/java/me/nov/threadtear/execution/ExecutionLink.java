package me.nov.threadtear.execution;

import me.nov.threadtear.execution.allatori.*;
import me.nov.threadtear.execution.analysis.*;
import me.nov.threadtear.execution.branchlock.CompatibilityStringObfuscationBranchLock;
import me.nov.threadtear.execution.cleanup.*;
import me.nov.threadtear.execution.cleanup.remove.*;
import me.nov.threadtear.execution.dasho.*;
import me.nov.threadtear.execution.generic.*;
import me.nov.threadtear.execution.generic.inliner.*;
import me.nov.threadtear.execution.paramorphism.*;
import me.nov.threadtear.execution.sb27.*;
import me.nov.threadtear.execution.skidfuscator.StringObfuscationSkidfuscator;
import me.nov.threadtear.execution.stringer.*;
import me.nov.threadtear.execution.tools.*;
import me.nov.threadtear.execution.generic.UniversalNumberObfuscation;
import me.nov.threadtear.execution.cleanup.remove.RemoveSignature;
import me.nov.threadtear.execution.zkm.*;

import java.util.ArrayList;
import java.util.List;

public class ExecutionLink {
  public static final List<Class<? extends Execution>> executions = new ArrayList<>() {{
    //Cleanup
    add(InlineMethods.class);
    add(InlineUnchangedFields.class);
    add(GuessParameterNames.class);
    add(Remapper.class);
    add(RemoveUnnecessary.class);
    add(RemoveUnusedVariables.class);
    add(RemoveAttributes.class);
    add(RemoveSignature.class);
    add(RemoveUnknownAttributes.class);
    add(RemoveInvalidPackage.class);

    //Generic
    add(ArgumentInliner.class);
    add(JSRInliner.class);
    add(ObfuscatedAccess.class);
    add(KnownConditionalJumps.class);
    add(ConvertCompareInstructions.class);
    add(UniversalNumberObfuscation.class);
    add(TryCatchObfuscationRemover.class);
    add(UnHider.class);
    add(StackOperationFixer.class);

    //Analysis
    add(RestoreSourceFiles.class);
    add(ReobfuscateClassNames.class);
    add(ReobfuscateMembers.class);
    add(ReobfuscateVariableNames.class);
    add(RemoveMonitors.class);
    add(RemoveTCBs.class);

    //SB27
    add(NumberPoolObfuscationSB27.class);
    add(FlowObfuscationSB27.class);
    add(SourceInfoStringObfuscationSB27.class);
    add(StringObfuscationSB27.class);
    add(StringPoolObfuscationSB27.class);
    add(InvokeDynamicObfuscationSB27.class);

    //Stringer
    add(StringObfuscationStringer.class);
    add(AccessObfuscationStringer.class);

    //ZKM
    add(StringObfuscationZKM.class);
    add(AccessObfuscationZKM.class);
    add(FlowObfuscationZKM.class);
    add(DESObfuscationZKM.class);
    add(NewStringObfuscationZKM.class);

    //Allatori
    add(StringObfuscationAllatori.class);
    add(ExpirationDateRemoverAllatori.class);
    add(JunkRemoverAllatori.class);

    //DashO
    add(StringObfuscationDashO.class);

    //Paramorphism
    add(BadAttributeRemover.class);
    add(StringObfuscationParamorphism.class);
    add(AccessObfuscationParamorphism.class);
    add(FlowObfuscationParamorphism.class);
    add(InvokeDynamicParamorphism.class);

    //BranchLock
    add(CompatibilityStringObfuscationBranchLock.class);

    //Skidfuscator
    add(StringObfuscationSkidfuscator.class);

    //Tools
    add(Java7Compatibility.class);
    add(Java8Compatibility.class);
    add(IsolatePossiblyMalicious.class);
    add(AddLineNumbers.class);
    add(LogAllExceptions.class);
    add(RemoveMaxs.class);

    //TODO: Plugins mby?
  }};
}
