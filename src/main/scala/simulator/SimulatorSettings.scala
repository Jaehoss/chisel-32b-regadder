import svsim.verilator
import verilator.Backend.CompilationSettings.TraceStyle



trait SimulatorSettings


trait TraceSetting extends SimulatorSettings {
  val traceStyle: TraceStyle
  val extension: String
}