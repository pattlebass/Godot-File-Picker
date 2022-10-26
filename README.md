# Godot-File-Picker

An Android plugin for Godot 3.5+ that allow you to open the native file dialog and choose a file. This file will be copied to `user://_temp/original_file_name`. After you are finished, you have to delete the temporary file yourself.

# API Reference

## Functions
> **Note**
> Argument cannot be `null`. [See a list of common MIME types.](https://developer.mozilla.org/en-US/docs/Web/HTTP/Basics_of_HTTP/MIME_types/Common_types)
```gdscript
openFilePicker(mimeType: String) -> void
```
## Signals
```gdscript
file_picked(String temp_path, String mime_type)
```


# Usage
```gdscript
var android_picker


func _ready() -> void:
    if Engine.has_singleton("GodotFilePicker"):
        android_picker = Engine.get_singleton("GodotFilePicker")
        android_picker.connect("file_picked", self, "_on_file_picked")


func _on_Button_pressed() -> void:
    # Call the file picker (with the specified type)
    android_picker.openFilePicker("*/*")


func _on_file_picked(temp_path: String, mime_type: String) -> void:
    print("Temporary path: " + temp_path)
    print("Mime type: " + mime_type)

    # Here you read the file or copy it to another directory

    # Now you can delete the temporary file
    var dir = Directory.new()
    dir.remove(temp_path)
```
