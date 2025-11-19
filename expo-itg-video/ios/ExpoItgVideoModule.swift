import ExpoModulesCore

public class ExpoItgVideoModule: Module {
    
    public func definition() -> ModuleDefinition {
        Name("ExpoItgVideoView")
        View(ExpoItgVideoView.self) {}
    }
    
}
