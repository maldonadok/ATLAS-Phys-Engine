# Take tranverse momentum values over mass to get velocity. Graph it.
import uproot
import awkward as ak
import pandas as pd

class EventObject:

    def __init__(self, filename):
        self.filename = filename
        self.tree = uproot.open({filename: "CollectionTree"})
        self.electrons = self.getElectrons()
        self.muons = self.getMuons()
        self.jets = self.getJets()
        self.event = self.getEvents()

    def getEvents(self):
        dfs = []
        dfs.extend([self.electrons,self.muons, self.jets])
        return pd.concat(dfs)

    def getElectrons(self):
        electrons = ak.to_dataframe(
            ak.zip(  # Zips multiple arrays into one single structure
                {
                    "pt": self.tree["AnalysisElectronsAuxDyn.pt"].array(),      # 0
                    "eta": self.tree["AnalysisElectronsAuxDyn.eta"].array(),    # 1
                    "phi": self.tree["AnalysisElectronsAuxDyn.phi"].array(),    # 2
                }
            )
        )
        return electrons

    def getMuons(self):
        muons = ak.to_dataframe(
            ak.zip(
                {
                    "pt": self.tree["AnalysisMuonsAuxDyn.pt"].array(),
                    "eta": self.tree["AnalysisMuonsAuxDyn.eta"].array(),
                    "phi": self.tree["AnalysisMuonsAuxDyn.phi"].array(),
                }
            )
        )
        return muons

    def getJets(self):
        jets = ak.to_dataframe(
            ak.zip(
                {
                    "pt": self.tree["AnalysisJetsAuxDyn.pt"].array(),
                    "eta": self.tree["AnalysisJetsAuxDyn.eta"].array(),
                    "phi": self.tree["AnalysisJetsAuxDyn.phi"].array(),
                    "mass": self.tree["AnalysisJetsAuxDyn.m"].array(),
                }
            )
        )
        return jets