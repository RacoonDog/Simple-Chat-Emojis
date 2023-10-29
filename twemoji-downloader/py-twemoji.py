import json
import os
import re
import shutil
import requests
import cairosvg
import subprocess
import time
from io import BytesIO
from PIL import Image

def compress_png(path):
    return subprocess.Popen(['pngout', path, '/y'], shell=True)

def download_and_convert_emoji(emoji, folder):
    identifier_name = re.sub(identifier_regex, '_', emoji['primaryName'])
    output_path = f"{folder}/assets/twemoji/textures/simple-chat-emojis/{identifier_name}.png"
    cairosvg.svg2png(url=emoji["assetUrl"], write_to=output_path, output_width=128, output_height=128)
    return compress_png(output_path)

if __name__ == '__main__':
    start = time.time()
    identifier_regex = r'[^a-z0-9_]'
    emojis = requests.get("https://emzi0767.gl-pages.emzi0767.dev/discord-emoji/discordEmojiMap-canary.json").json()
    folder = os.path.join(os.getcwd(), os.pardir, 'src/main/resources/resourcepacks/twemoji-emoji-pack')

    try:
        os.makedirs(f"{folder}/assets/twemoji/textures/simple-chat-emojis")
    except FileExistsError:
        pass

    print("Getting emojis")
    processes = []
    for emoji in emojis["emojiDefinitions"]:
        print(emoji["assetUrl"])
        p = download_and_convert_emoji(emoji, folder)
        processes.append(p)

    mcmeta = {"pack": {"pack_format": 18, "description": "Default emojis from Discord"}}

    with open(f"{folder}/pack.mcmeta", "w") as mcmeta_file:
        json.dump(mcmeta, mcmeta_file, indent=2)

    response = requests.get(f"https://discord.com/assets/847541504914fd33810e70a0ea73177e.ico")

    if response.status_code == 200:
        icon_path = f"{folder}/pack.png"
        with Image.open(BytesIO(response.content)) as img:
            img.save(icon_path)
        p = compress_png(icon_path)
        processes.append(p)

    for process in processes:
        process.wait()

    seconds = round(time.time() - start, 2)
    print(f"Done in {seconds} seconds.")
