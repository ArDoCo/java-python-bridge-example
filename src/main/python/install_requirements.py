import os
import sys
import subprocess
import json

def read_config(path):
    with open(path) as json_file:
        conf = json.load(json_file)
    
    return conf

def main():
    conf = read_config('./src/main/resources/conf.json')
    req_file_path = conf['reqFile']
    target_path = conf['targetPath']
    
    if target_path == '':
        home_dir = os.path.expanduser('~')
        target_path = os.path.join(home_dir,'exampleProject_pythonLibraries')

    install_reqs(req_file_path, target_path)

def install_reqs(req_file_path, target_path):
    
    if not os.path.exists(target_path):
        os.makedirs(target_path)
    
    subprocess.run([sys.executable, '-m', 'pip', 'install', '-r', req_file_path, '-t', target_path])

if __name__ == "__main__":
    main()