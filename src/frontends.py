from kivy.lang import Builder

from kivymd.app import MDApp
from kivymd.uix.menu import MDDropdownMenu


KV = '''
#:import CustomOverFlowMenu __main__.CustomOverFlowMenu

MDBoxLayout:
    orientation: "vertical"
    md_bg_color: "#FFEFD3"


    MDTopAppBar:
        title: "Menu"
        md_bg_color: "#FFA500"
        left_action_items: [["menu", lambda x: nav_draw.set_state()]]

    MDNavigationLayout:
        ScreenManager:
            id: screen_manager
            Screen:
                Label:
                    text: "Welcome to the app"
                    halign: "center"
                    font_size: "40sp"
                    font_style: "Button"
                    pos_hint: {"center_x": .5, "center_y": .70}
                    font_name: "DejaVuSans.ttf"
                    color: "#FFA500"
                MDTextField:
                    hint_text: 'Enter your password'
                    helper_text: 'Forgot your password?'
                    helper_text_mode: "on_focus"
                    pos_hint: {'center_x': 0.5, 'center_y': 0.45}
                    size_hint_x: None
                    width: 300
                    icon_right: "account-search"
                    required: True
                MDTextField:
                    hint_text: 'Enter your username'
                    helper_text_mode: "on_focus"
                    pos_hint: {'center_x': 0.5, 'center_y': 0.55}
                    size_hint_x: None
                    width: 300
                    required: True
            Screen:
                name: "screen 1"
                MDTextField:
                    hint_text: 'Enter the language'
                    helper_text: 'Default language is English'
                    helper_text_mode: "on_focus"
                    pos_hint: {'right_x': 0.25, 'up_y': 0.5}
                    size_hint_x: None
                    width: 200
                    icon_right: "language"
                    required: True
                    mode: "rectangle"
                    on_text_validate: app.show_language_menu()
                MDTextField:
                    hint_text: 'Enter the text you want to translate'
                    width: 300
                    length: 400
                    pos_hint: {'right_x': 0.25, 'right_y': 0.3}
                    widget_multiline: False
                    widget_height: 100
                    widget_width: 300
                MDTextField:
                    hint_text: 'Enter the language'
                    helper_text: 'Default language is English'
                    helper_text_mode: "on_focus"
                    pos_hint: {'center_x': 0.75, 'center_y': 0.5}
                    size_hint_x: None
                    width: 300
                    icon_right: "language"
                    required: True
                    mode: "rectangle"
                MDTextField:
                    color: "#FFFFFF"
                    width: 300
                    length: 400
                    pos_hint: {'center_x': 0.75, 'center_y': 0.5}
            Screen:
                name: "screen 2"
                orientation: 'vertical'
                MDBottomAppBar:
                    md_bg_color: "#F02D3A"
                    MDTopAppBar:
                        icon: "circle"
                        type: "custom"
                        type: "bottom"
                        icon_color: "#F02D3A"
                        on_action_button: root.capture()
                    Camera:
                        id: camera
                        resolution: (640, 480)
                        play: False

            AnchorLayout:
                anchor_x: "left"
                size_hint_y: None
                height: avatar.height
                Image:
                    id: avatar
                    size_hint: None, None
                    size: "56dp", "56dp"
                    source: "data/logo/kivy-icon-256.png"

            MDLabel:
                text: "Akonadi"
                font_style: "Button"
                size_hint_y: None
                height: self.texture_size[1]
            MDLabel:
                text: "your@gmail.com"
                font_style: "Caption"
                size_hint_y: None
                height: self.texture_size[1]
            ScrollView:
                MDList:
                    OneLineAvatarListItem:
                        on_press:
                            nav_draw.set_state("close")
                            screen_manager.current = "screen 1"
                        text:"Home"
                        IconLeftWidget:
                            icon: "home"
                    OneLineAvatarListItem:
                        on_press:
                            nav_draw.set_state("close")
                            screen_manager.current = "screen 2"
                        text:"Camera translator"
                        IconLeftWidget:
                            icon: "camera"

'''


class CustomOverFlowMenu(MDDropdownMenu):
    def __init__(self, **kwargs):
        super().__init__(**kwargs)
        self.register_event_type('on_release')
    pass


def callback(instance_action_top_appbar_button):
    print(instance_action_top_appbar_button)


class Test(MDApp):
    def build(self):
        return Builder.load_string(KV)


Test().run()
