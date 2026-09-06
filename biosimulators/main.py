import argparse
from py4j.java_gateway import JavaGateway
from biosimulators_utils.combine.exec import exec_sedml_docs_in_archive

def exec_sed_doc(doc, working_dir, base_out_path, rel_out_path=None, apply_xml_model_changes=False, log=None, indent=0, out_dir=None, config=None):
    pass

def exec_sed_task(task, variables, preprocessed_task=None, log=None, config=None):
    gateway = JavaGateway()
    sbscl_engine = gateway.entry_point
    return {}, log

def main():
    parser = argparse.ArgumentParser(description="SBSCL BioSimulators interface")
    parser.add_argument("-i", "--archive", required=True, type=str)
    parser.add_argument("-o", "--out-dir", required=True, type=str)
    args = parser.parse_args()

    exec_sedml_docs_in_archive(
        exec_sed_doc,
        args.archive,
        args.out_dir,
        apply_xml_model_changes=True,
    )

if __name__ == "__main__":
    main()