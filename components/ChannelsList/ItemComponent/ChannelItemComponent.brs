sub init()
    m.titleLabel = m.top.findNode("titleLabel")
    m.titleLabel.font = getMediumFont(35)
    m.background = m.top.findNode("background")
    m.colorInterpolator = m.top.findNode("colorInterpolator")
end sub

sub configureDataSource()
    item = m.top.itemContent.title
    m.titleLabel.text = item
end sub

sub itemFocused()
    if m.top.rowListHasFocus
        m.colorInterpolator.fraction = m.top.rowFocusPercent
    else
        m.colorInterpolator.fraction = 0
    end if
end sub

sub layoutSubviews()
    m.titleLabel.height = m.top.height
    m.titleLabel.width = m.top.width
    m.background.height = m.top.height
    m.background.width = m.top.width
end sub