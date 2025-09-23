from kivy.app import App
from kivy.lang import Builder
from kivy.uix.boxlayout import BoxLayout
Builder.load_string('''
ScreenManager:
    
<login>:
    orientation: 'vertical'
    padding: 10
    spacing: 10
    MDLabel:
        text: 'Login'
        halign: 'center'
        font_style: 'H4'
        theme_text_color: 'Custom'
        text_color: 0, 0, 0, 1
    MDTextField:
        id: username
        hint_text: 'Username'
        helper_text: 'or email'
        helper_text_mode: 'on_focus'
        icon_right: 'account'
        icon_right_color: app.theme_cls.primary_color
        required: True
    MDTextField:
        id: password
        hint_text: 'Password'
        helper_text: 'Forgot password?'
        helper_text_mode: 'on_focus'
        icon_right: 'eye-off'
        icon_right_color: app.theme_cls.primary_color
        required: True
        password: True
    MDRaisedButton:
        text: 'Login'
        pos_hint: {'center_x': .5}
        on_release: app.login()
        
<CameraClick>:
    orientation: 'vertical'
    Camera:
        id: camera
        resolution: (640, 480)
        play: False
    ToggleButton:
        text: 'Play'
        on_press: camera.play = not camera.play
        size_hint_y: None
        height: '48dp'
    Button:
        text: 'Capture'
        size_hint_y: None
        height: '48dp'
        pos_hint: {'center_x': .5}
        icon: "circle"
        type: "custom"
        on_press: root.capture() 
''')


class CameraClick(BoxLayout):
    def capture(self):
        '''
        Function to capture the images and give them the names
        according to their captured time and date.
        '''
        camera = self.ids['camera']
        camera.export_to_png("IMG.png")
        print("Captured")
        self.manager.current = 'second'


class TestCamera(App):

    def build(self):
        return CameraClick()


TestCamera().run()